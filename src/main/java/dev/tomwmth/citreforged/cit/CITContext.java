package dev.tomwmth.citreforged.cit;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Holds momentary information to be used for CITs' condition matching and type effects.
 */
public class CITContext {
    /**
     * The main item stack to check for the CIT.
     */
    public final ItemStack stack;

    /**
     * The item's containing world(defaults to {@link Minecraft#level} if null)
     */
    public final Level world;

    /**
     * The item's associated living entity if present. (null if not relevant)
     */
    public final LivingEntity entity;

    /**
     * Cached enchantment map from {@link #stack}.
     * @see #enchantments()
     */
    private Map<ResourceLocation, Integer> enchantments = null;

    public CITContext(ItemStack stack, Level world, LivingEntity entity) {
        this.stack = stack;
        this.world = world == null ? Minecraft.getInstance().level : world;
        this.entity = entity;
    }

    /**
     * @see #enchantments
     * @return a map of this context item's enchantments
     */
    public Map<ResourceLocation, Integer> enchantments() {
        if (this.enchantments == null) {
            this.enchantments = new LinkedHashMap<>();
            for (Tag nbtElement : stack.is(Items.ENCHANTED_BOOK) ? EnchantedBookItem.getEnchantments(stack) : stack.getEnchantmentTags()) {
                CompoundTag nbtCompound = (CompoundTag) nbtElement;
                this.enchantments.put(EnchantmentHelper.getEnchantmentId(nbtCompound), EnchantmentHelper.getEnchantmentLevel(nbtCompound));
            }
        }
        return this.enchantments;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CITContext that &&
                Objects.equals(this.stack, that.stack) &&
                Objects.equals(this.world, that.world) &&
                Objects.equals(this.entity, that.entity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stack, world, entity);
    }
}
