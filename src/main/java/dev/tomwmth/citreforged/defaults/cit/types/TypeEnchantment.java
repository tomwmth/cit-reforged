package dev.tomwmth.citreforged.defaults.cit.types;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.tomwmth.citreforged.api.CITGlobalProperties;
import dev.tomwmth.citreforged.api.CITTypeContainer;
import dev.tomwmth.citreforged.cit.*;
import dev.tomwmth.citreforged.defaults.config.CITResewnDefaultsConfig;
import dev.tomwmth.citreforged.mixin.defaults.types.enchantment.RenderBuffersAccessor;
import dev.tomwmth.citreforged.mixin.defaults.types.enchantment.RenderStateShardAccessor;
import dev.tomwmth.citreforged.pack.format.PropertyGroup;
import dev.tomwmth.citreforged.pack.format.PropertyKey;
import dev.tomwmth.citreforged.pack.format.PropertyValue;
import dev.tomwmth.citreforged.util.logic.Loops;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import dev.tomwmth.citreforged.defaults.cit.conditions.ConditionEnchantments;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;

import static com.mojang.blaze3d.systems.RenderSystem.*;
import static org.lwjgl.opengl.GL11.*;

public class TypeEnchantment extends CITType {
    public static final Container CONTAINER = new Container();

    @Override
    public Set<PropertyKey> typeProperties() {
        return Set.of(
                PropertyKey.of("texture"),
                PropertyKey.of("layer"),
                PropertyKey.of("speed"),
                PropertyKey.of("rotation"),
                PropertyKey.of("duration"),
                PropertyKey.of("r"),
                PropertyKey.of("g"),
                PropertyKey.of("b"),
                PropertyKey.of("a"),
                PropertyKey.of("useGlint"),
                PropertyKey.of("blur"),
                PropertyKey.of("blend"));
    }

    public ResourceLocation texture;
    public int layer;
    public float speed, rotation, duration, r, g, b, a;
    public boolean useGlint, blur;
    public Blend blend;

    public final Map<GlintRenderLayer, RenderType> renderLayers = new EnumMap<>(GlintRenderLayer.class);
    private final MergeMethodIntensity methodIntensity = new MergeMethodIntensity();
    private Set<ResourceLocation> enchantmentChecks = null;

    @Override
    public void load(List<CITCondition> conditions, PropertyGroup properties, ResourceManager resourceManager) throws CITParsingException {
        PropertyValue textureProp = properties.getLastWithoutMetadata("citresewn", "texture");
        this.texture = resolveAsset(properties.identifier, textureProp, "textures", ".png", resourceManager);
        if (this.texture == null)
            throw textureProp == null ? new CITParsingException("No texture specified", properties, -1) : new CITParsingException("Could not resolve texture", properties, textureProp.position());

        PropertyValue layerProp = properties.getLastWithoutMetadataOrDefault("0", "citresewn", "layer");
        try {
            this.layer = Integer.parseInt(layerProp.value());
        } catch (Exception e) {
            throw new CITParsingException("Could not parse integer", properties, layerProp.position(), e);
        }

        this.speed = parseFloatOrDefault(1f, "speed", properties);
        this.rotation = parseFloatOrDefault(10f, "rotation", properties);
        this.duration = Math.max(0f, parseFloatOrDefault(0f, "duration", properties));
        this.r = Math.max(0f, parseFloatOrDefault(1f, "r", properties));
        this.g = Math.max(0f, parseFloatOrDefault(1f, "g", properties));
        this.b = Math.max(0f, parseFloatOrDefault(1f, "b", properties));
        this.a = Math.max(0f, parseFloatOrDefault(1f, "a", properties));

        this.useGlint = Boolean.parseBoolean(properties.getLastWithoutMetadataOrDefault("false", "citresewn", "useGlint").value());
        this.blur = Boolean.parseBoolean(properties.getLastWithoutMetadataOrDefault("true", "citresewn", "blur").value());

        PropertyValue blendProp = properties.getLastWithoutMetadataOrDefault("add", "citresewn", "blend");
        try {
            this.blend = Blend.getBlend(blendProp.value());
        } catch (Exception e) {
            throw new CITParsingException("Could not parse blending method", properties, blendProp.position(), e);
        }

        for (CITCondition condition : conditions)
            if (condition instanceof ConditionEnchantments enchantments) {
                if (enchantmentChecks == null && enchantments.getEnchantments().length > 0)
                    enchantmentChecks = new HashSet<>();

                enchantmentChecks.addAll(Arrays.asList(enchantments.getEnchantments()));
            }
    }

    private float parseFloatOrDefault(float defaultValue, String propertyName, PropertyGroup properties) throws CITParsingException {
        PropertyValue property = properties.getLastWithoutMetadata("citresewn", propertyName);
        if (property == null)
            return defaultValue;
        try {
            return Float.parseFloat(property.value());
        } catch (Exception e) {
            throw new CITParsingException("Could not parse float", properties, property.position(), e);
        }
    }

    public static class Container extends CITTypeContainer<TypeEnchantment> implements CITGlobalProperties {
        public Container() {
            super(TypeEnchantment.class, TypeEnchantment::new, "enchantment");
        }

        public boolean globalUseGlint = true;
        public int globalCap = Integer.MAX_VALUE;
        public MergeMethodIntensity.MergeMethod globalMergeMethod = MergeMethodIntensity.MergeMethod.AVERAGE;
        public float globalFade = 0.5f;

        public List<CIT<TypeEnchantment>> loaded = new ArrayList<>();
        public List<List<CIT<TypeEnchantment>>> loadedLayered = new ArrayList<>();

        private List<CIT<TypeEnchantment>> appliedContext = null;
        private boolean apply = false, defaultGlint = false;

        @Override
        public void load(List<CIT<TypeEnchantment>> parsedCITs) {
            loaded.addAll(parsedCITs);

            Map<Integer, List<CIT<TypeEnchantment>>> layers = new HashMap<>();
            for (CIT<TypeEnchantment> cit : loaded)
                layers.computeIfAbsent(cit.type.layer, i -> new ArrayList<>()).add(cit);
            loadedLayered.clear();
            layers.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEachOrdered(layer -> loadedLayered.add(layer.getValue()));

            for (CIT<TypeEnchantment> cit : loaded)
                for (GlintRenderLayer glintLayer : GlintRenderLayer.values()) {
                    RenderType renderLayer = glintLayer.build(cit.type, cit.propertiesIdentifier);

                    cit.type.renderLayers.put(glintLayer, renderLayer);
                    ((RenderBuffersAccessor) Minecraft.getInstance().renderBuffers()).getFixedBuffers().put(renderLayer, new BufferBuilder(renderLayer.bufferSize()));
                }
        }

        @Override
        public void globalProperty(String key, PropertyValue value) throws Exception {
            switch (key) {
                case "useGlint" -> {
                    globalUseGlint = value == null || Boolean.parseBoolean(value.value());
                    if (!globalUseGlint && !"false".equalsIgnoreCase(value.value()))
                        throw new Exception("Could not parse boolean");
                }
                case "cap" -> {
                    globalCap = value == null ? Integer.MAX_VALUE : Integer.parseInt(value.value());
                }
                case "method" -> {
                    globalMergeMethod = value == null ? MergeMethodIntensity.MergeMethod.AVERAGE : MergeMethodIntensity.MergeMethod.parse(value.value());
                }
                case "fade" -> {
                    globalFade = value == null ? 0.5f : Float.parseFloat(value.value());
                }
            }
        }

        @Override
        public void dispose() {
            appliedContext = null;
            apply = false;

            for (CIT<TypeEnchantment> cit : loaded)
                for (RenderType renderLayer : cit.type.renderLayers.values())
                    ((RenderBuffersAccessor) Minecraft.getInstance().renderBuffers()).getFixedBuffers().remove(renderLayer);

            loaded.clear();
            loadedLayered.clear();
        }

        public void apply() {
            if (appliedContext != null)
                apply = true;
        }

        public boolean shouldApply() {
            return apply && active();
        }

        public boolean shouldNotApplyDefaultGlint() {
            return !globalUseGlint || (apply && !defaultGlint);
        }

        public Container setContext(CITContext context) {
            apply = false;
            defaultGlint = false;
            appliedContext = null;
            if (context == null)
                return this;

            List<WeakReference<CIT<TypeEnchantment>>> cits = ((CITCacheEnchantment) (Object) context.stack).citresewn$getCacheTypeEnchantment().get(context);

            appliedContext = new ArrayList<>();
            if (cits != null)
                for (WeakReference<CIT<TypeEnchantment>> citRef : cits)
                    if (citRef != null) {
                        CIT<TypeEnchantment> cit = citRef.get();
                        if (cit != null) {
                            appliedContext.add(cit);
                            if (cit.type.useGlint)
                                defaultGlint = true;
                        }
                    }

            if (appliedContext.isEmpty())
                appliedContext = null;
            else
                globalMergeMethod.applyMethod(appliedContext, context);

            return this;
        }

        public List<CIT<TypeEnchantment>> getRealTimeCIT(CITContext context) {
            List<CIT<TypeEnchantment>> cits = new ArrayList<>();
            for (List<CIT<TypeEnchantment>> layer : loadedLayered)
                for (CIT<TypeEnchantment> cit : layer)
                    if (cit.test(context)) {
                        cits.add(cit);
                        break;
                    }

            return cits;
        }
    }

    public enum GlintRenderLayer {
        ARMOR_GLINT("armor_glint", 8f, layer -> layer
                .setShaderState(RenderStateShardAccessor.ARMOR_GLINT_SHADER())
                .setWriteMaskState(RenderStateShardAccessor.COLOR_MASK())
                .setCullState(RenderStateShardAccessor.DISABLE_CULLING())
                .setDepthTestState(RenderStateShardAccessor.EQUAL_DEPTH_TEST())
                .setLayeringState(RenderStateShardAccessor.VIEW_OFFSET_Z_LAYERING())),
        ARMOR_ENTITY_GLINT("armor_entity_glint", 0.16f, layer -> layer
                .setShaderState(RenderStateShardAccessor.ARMOR_ENTITY_GLINT_SHADER())
                .setWriteMaskState(RenderStateShardAccessor.COLOR_MASK())
                .setCullState(RenderStateShardAccessor.DISABLE_CULLING())
                .setDepthTestState(RenderStateShardAccessor.EQUAL_DEPTH_TEST())
                .setLayeringState(RenderStateShardAccessor.VIEW_OFFSET_Z_LAYERING())),
        GLINT_TRANSLUCENT("glint_translucent", 8f, layer -> layer
                .setShaderState(RenderStateShardAccessor.TRANSLUCENT_GLINT_SHADER())
                .setWriteMaskState(RenderStateShardAccessor.COLOR_MASK())
                .setCullState(RenderStateShardAccessor.DISABLE_CULLING())
                .setDepthTestState(RenderStateShardAccessor.EQUAL_DEPTH_TEST())
                .setOutputState(RenderStateShardAccessor.ITEM_TARGET())),
        GLINT("glint", 8f, layer -> layer
                .setShaderState(RenderStateShardAccessor.GLINT_SHADER())
                .setWriteMaskState(RenderStateShardAccessor.COLOR_MASK())
                .setCullState(RenderStateShardAccessor.DISABLE_CULLING())
                .setDepthTestState(RenderStateShardAccessor.EQUAL_DEPTH_TEST())),
        DIRECT_GLINT("glint_direct", 8f, layer -> layer
                .setShaderState(RenderStateShardAccessor.DIRECT_GLINT_SHADER())
                .setWriteMaskState(RenderStateShardAccessor.COLOR_MASK())
                .setCullState(RenderStateShardAccessor.DISABLE_CULLING())
                .setDepthTestState(RenderStateShardAccessor.EQUAL_DEPTH_TEST())),
        ENTITY_GLINT("entity_glint", 0.16f, layer -> layer
                .setShaderState(RenderStateShardAccessor.ENTITY_GLINT_SHADER())
                .setWriteMaskState(RenderStateShardAccessor.COLOR_MASK())
                .setCullState(RenderStateShardAccessor.DISABLE_CULLING())
                .setDepthTestState(RenderStateShardAccessor.EQUAL_DEPTH_TEST())
                .setOutputState(RenderStateShardAccessor.ITEM_TARGET())),
        DIRECT_ENTITY_GLINT("entity_glint_direct", 0.16f, layer -> layer
                .setShaderState(RenderStateShardAccessor.DIRECT_ENTITY_GLINT_SHADER())
                .setWriteMaskState(RenderStateShardAccessor.COLOR_MASK())
                .setCullState(RenderStateShardAccessor.DISABLE_CULLING())
                .setDepthTestState(RenderStateShardAccessor.EQUAL_DEPTH_TEST()));

        public final String name;
        private final Consumer<RenderType.CompositeState.CompositeStateBuilder> setup;
        private final float scale;

        GlintRenderLayer(String name, float scale, Consumer<RenderType.CompositeState.CompositeStateBuilder> setup) {
            this.name = name;
            this.scale = scale;
            this.setup = setup;
        }

        public RenderType build(TypeEnchantment enchantment, ResourceLocation propertiesResourceLocation) {
            class Texturing implements Runnable {
                private final float speed, rotation, r, g, b, a;
                private final MergeMethodIntensity methodIntensity;

                Texturing(float speed, float rotation, float r, float g, float b, float a, MergeMethodIntensity methodIntensity) {
                    this.speed = speed;
                    this.rotation = rotation;
                    this.r = r;
                    this.g = g;
                    this.b = b;
                    this.a = a;
                    this.methodIntensity = methodIntensity;
                }

                @Override
                public void run() {
                    float l = Util.getMillis() * CITResewnDefaultsConfig.INSTANCE.type_enchantment_scroll_multiplier * speed;
                    float x = (l % 110000f) / 110000f;
                    float y = (l % 30000f) / 30000f;
                    setTextureMatrix(new Matrix4f()
                            .translation(-x, y, 0.0F)
                            .rotateZ(rotation)
                            .scale(scale));

                    setShaderColor(r, g, b, a * methodIntensity.intensity);
                }
            }

            RenderType.CompositeState.CompositeStateBuilder layer = RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(enchantment.texture, enchantment.blur, false))
                    .setTexturingState(new RenderStateShard.TexturingStateShard("citresewn_glint_texturing", new Texturing(enchantment.speed, enchantment.rotation, enchantment.r, enchantment.g, enchantment.b, enchantment.a, enchantment.methodIntensity), () -> {
                        RenderSystem.resetTextureMatrix();

                        setShaderColor(1f, 1f, 1f, 1f);
                    }))
                    .setTransparencyState(enchantment.blend);

            this.setup.accept(layer);

            return RenderType.create("citresewn:enchantment_" + this.name + ":" + propertiesResourceLocation.toString(),
                    DefaultVertexFormat.POSITION_TEX,
                    VertexFormat.Mode.QUADS,
                    256,
                    layer.createCompositeState(false));
        }

        public VertexConsumer tryApply(VertexConsumer base, RenderType baseLayer, MultiBufferSource provider) {
            if (!CONTAINER.apply || CONTAINER.appliedContext == null || CONTAINER.appliedContext.isEmpty())
                return null;

            VertexConsumer[] layers = new VertexConsumer[Math.min(CONTAINER.appliedContext.size(), CONTAINER.globalCap)];

            for (int i = 0; i < layers.length; i++)
                layers[i] = provider.getBuffer(CONTAINER.appliedContext.get(i).type.renderLayers.get(GlintRenderLayer.this));

            provider.getBuffer(baseLayer); // refresh base layer for armor consumer

            return base == null ? VertexMultiConsumer.create(layers) : VertexMultiConsumer.create(VertexMultiConsumer.create(layers), base);
        }
    }

    public static class MergeMethodIntensity {
        public float intensity = 1f;

        public enum MergeMethod {
            NONE,
            AVERAGE {
                @Override
                public void applyIntensity(Map<ResourceLocation, Integer> stackEnchantments, CIT<TypeEnchantment> cit) {
                    ResourceLocation enchantment = null;
                    for (ResourceLocation enchantmentMatch : cit.type.enchantmentChecks)
                        if (stackEnchantments.containsKey(enchantmentMatch)) {
                            enchantment = enchantmentMatch;
                            break;
                        }

                    if (enchantment == null) {
                        cit.type.methodIntensity.intensity = 0f;
                    } else {
                        float sum = 0f;
                        for (Integer value : stackEnchantments.values())
                            sum += value;

                        cit.type.methodIntensity.intensity = (float) stackEnchantments.get(enchantment) / sum;
                    }
                }
            },
            LAYERED {
                @Override
                public void applyIntensity(Map<ResourceLocation, Integer> stackEnchantments, CIT<TypeEnchantment> cit) {
                    ResourceLocation enchantment = null;
                    for (ResourceLocation enchantmentMatch : cit.type.enchantmentChecks)
                        if (stackEnchantments.containsKey(enchantmentMatch)) {
                            enchantment = enchantmentMatch;
                            break;
                        }
                    if (enchantment == null) {
                        cit.type.methodIntensity.intensity = 0f;
                        return;
                    }

                    float max = 0f;
                    for (Integer value : stackEnchantments.values())
                        if (value > max)
                            max = value;

                    cit.type.methodIntensity.intensity = (float) stackEnchantments.get(enchantment) / max;
                }
            },
            CYCLE {
                @Override
                public void applyMethod(List<CIT<TypeEnchantment>> citEnchantments, CITContext context) {
                    List<Map.Entry<CIT<TypeEnchantment>, Float>> durations = new ArrayList<>();
                    for (CIT<TypeEnchantment> cit : citEnchantments)
                        durations.add(new HashMap.SimpleEntry<>(cit, cit.type.duration));

                    for (Map.Entry<CIT<TypeEnchantment>, Float> intensity : Loops.statelessFadingLoop(durations, CONTAINER.globalFade, ticks, 20).entrySet())
                        intensity.getKey().type.methodIntensity.intensity = intensity.getValue();
                }
            };

            public static int ticks = 0;

            public void applyIntensity(Map<ResourceLocation, Integer> stackEnchantments, CIT<TypeEnchantment> cit) {
                cit.type.methodIntensity.intensity = 1f;
            }

            public void applyMethod(List<CIT<TypeEnchantment>> citEnchantments, CITContext context) {
                Map<ResourceLocation, Integer> stackEnchantments = context.enchantments();

                for (CIT<TypeEnchantment> cit : citEnchantments)
                    if (cit.type.enchantmentChecks != null)
                        applyIntensity(stackEnchantments, cit);
            }

            public static MergeMethod parse(String value) {
                return switch (value.toLowerCase(Locale.ROOT)) {
                    case "none" -> NONE;
                    case "average" -> AVERAGE;
                    case "layered" -> LAYERED;
                    case "cycle" -> CYCLE;
                    default -> throw new IllegalArgumentException("Unknown merge method");
                };
            }
        }
    }

    public static class Blend extends RenderStateShard.TransparencyStateShard {
        private final int src, dst, srcAlpha, dstAlpha;

        private Blend(String name, int src, int dst, int srcAlpha, int dstAlpha) {
            super(name + "_glint_transparency", null, null);
            this.src = src;
            this.dst = dst;
            this.srcAlpha = srcAlpha;
            this.dstAlpha = dstAlpha;
        }

        private Blend(String name, int src, int dst) {
            this(name, src, dst, GL_ZERO, GL_ONE);
        }

        @Override
        public void setupRenderState() {
            enableBlend();
            blendFuncSeparate(src, dst, srcAlpha, dstAlpha);
        }

        @Override
        public void clearRenderState() {
            defaultBlendFunc();
            disableBlend();
        }

        public static Blend getBlend(String blendString) throws Exception {
            try { //check named blending function
                return Named.valueOf(blendString.toUpperCase(Locale.ENGLISH)).blend;
            } catch (IllegalArgumentException ignored) { // create custom blending function
                String[] split = blendString.split("\\p{Zs}+");
                int src, dst, srcAlpha, dstAlpha;
                if (split.length == 2) {
                    src = parseGLConstant(split[0]);
                    dst = parseGLConstant(split[1]);
                    srcAlpha = GL_ZERO;
                    dstAlpha = GL_ONE;
                } else if (split.length == 4) {
                    src = parseGLConstant(split[0]);
                    dst = parseGLConstant(split[1]);
                    srcAlpha = parseGLConstant(split[2]);
                    dstAlpha = parseGLConstant(split[3]);
                } else
                    throw new Exception();

                return new Blend("custom_" + src + "_" + dst + "_" + srcAlpha + "_" + dstAlpha, src, dst, srcAlpha, dstAlpha);
            }
        }

        private enum Named {
            REPLACE(new Blend("replace", 0, 0) {
                @Override
                public void setupRenderState() {
                    disableBlend();
                }
            }),
            GLINT(new Blend("glint", GL_SRC_COLOR, GL_ONE)),
            ALPHA(new Blend("alpha", GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)),
            ADD(new Blend("add", GL_SRC_ALPHA, GL_ONE)),
            SUBTRACT(new Blend("subtract", GL_ONE_MINUS_DST_COLOR, GL_ZERO)),
            MULTIPLY(new Blend("multiply", GL_DST_COLOR, GL_ONE_MINUS_SRC_ALPHA)),
            DODGE(new Blend("dodge", GL_ONE, GL_ONE)),
            BURN(new Blend("burn", GL_ZERO, GL_ONE_MINUS_SRC_COLOR)),
            SCREEN(new Blend("screen", GL_ONE, GL_ONE_MINUS_SRC_COLOR)),
            OVERLAY(new Blend("overlay", GL_DST_COLOR, GL_SRC_COLOR));

            public final Blend blend;

            Named(Blend blend) {
                this.blend = blend;
            }
        }

        private static int parseGLConstant(String s) throws Exception {
            try {
                return GL11.class.getDeclaredField(s).getInt(null);
            } catch (NoSuchFieldException ignored) { }

            return s.startsWith("0x") ? Integer.parseInt(s.substring(2), 16) : Integer.parseInt(s);
        }
    }

    public interface CITCacheEnchantment {
        CITCache.MultiList<TypeEnchantment> citresewn$getCacheTypeEnchantment();
    }
}
