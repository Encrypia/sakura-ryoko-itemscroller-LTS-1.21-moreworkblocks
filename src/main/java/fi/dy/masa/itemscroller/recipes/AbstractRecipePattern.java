package fi.dy.masa.itemscroller.recipes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import fi.dy.masa.itemscroller.util.InventoryUtils;

public abstract class AbstractRecipePattern
{
    public enum RecipeType
    {
        CRAFTING("crafting"),
        STONECUTTER("stonecutter"),
        ANVIL("anvil"),
        GRINDSTONE("grindstone"),
        LOOM("loom"),
        SMITHING("smithing"),
        ENCHANTMENT("enchantment");

        private final String id;

        RecipeType(String id) { this.id = id; }
        public String getId() { return this.id; }

        @Nullable
        public static RecipeType fromId(String id)
        {
            for (RecipeType t : values())
            {
                if (t.id.equals(id)) return t;
            }
            return null;
        }
    }

    protected ItemStack result = InventoryUtils.EMPTY_STACK;

    public abstract RecipeType getType();

    public abstract void clearRecipe();

    public abstract boolean isEmpty();

    public abstract void storeRecipe(Slot slot, HandledScreen<? extends ScreenHandler> gui,
                                     boolean clearIfEmpty, boolean fromKeybind, MinecraftClient mc);

    public abstract void readFromNBT(@Nonnull NbtCompound nbt, @Nonnull DynamicRegistryManager registryManager);

    @Nonnull
    public abstract NbtCompound writeToNBT(@Nonnull DynamicRegistryManager registryManager);

    public ItemStack getResult()
    {
        return this.result.isEmpty() ? InventoryUtils.EMPTY_STACK : this.result;
    }

    public boolean isValid()
    {
        return InventoryUtils.isStackEmpty(this.getResult()) == false;
    }

    @Nullable
    public Slot getOutputSlot(HandledScreen<? extends ScreenHandler> gui)
    {
        return null;
    }

    public boolean canCraft(HandledScreen<? extends ScreenHandler> gui)
    {
        return this.isValid() && this.getOutputSlot(gui) != null;
    }

    public void fillInputs(HandledScreen<? extends ScreenHandler> gui, boolean fillStacks, Slot craftingOutputSlot)
    {
    }

    public void clearInputs(HandledScreen<? extends ScreenHandler> gui)
    {
    }

    @Nonnull
    public ItemStack[] getInputStacksForDisplay()
    {
        return new ItemStack[0];
    }

    @Nullable
    public String getDisplayText()
    {
        return null;
    }

    public void craftAsManyAsPossible(HandledScreen<? extends ScreenHandler> gui)
    {
        Slot outputSlot = this.getOutputSlot(gui);
        if (outputSlot == null || this.isValid() == false) return;

        ItemStack resultStack = this.getResult();
        int failSafe = 1024;

        while (failSafe > 0 && outputSlot.hasStack() &&
                InventoryUtils.areStacksEqual(outputSlot.getStack(), resultStack))
        {
            InventoryUtils.dropStacksWhileHasItem(gui, outputSlot.id, resultStack);

            if (outputSlot.hasStack() == false ||
                InventoryUtils.areStacksEqual(outputSlot.getStack(), resultStack) == false)
            {
                this.fillInputs(gui, true, outputSlot);
            }
            else
            {
                break;
            }

            failSafe--;
        }
    }

    public void craftAsManyAndKeep(HandledScreen<? extends ScreenHandler> gui)
    {
        Slot outputSlot = this.getOutputSlot(gui);
        if (outputSlot == null || this.isValid() == false) return;

        ItemStack resultStack = this.getResult();
        int failSafe = 1024;

        while (failSafe > 0 && outputSlot.hasStack() &&
                InventoryUtils.areStacksEqual(outputSlot.getStack(), resultStack))
        {
            InventoryUtils.shiftClickSlot(gui, outputSlot.id);

            if (outputSlot.hasStack() == false ||
                InventoryUtils.areStacksEqual(outputSlot.getStack(), resultStack) == false)
            {
                this.fillInputs(gui, true, outputSlot);
            }
            else
            {
                break;
            }

            failSafe--;
        }
    }

    public void craftEverything(HandledScreen<? extends ScreenHandler> gui)
    {
        Slot outputSlot = this.getOutputSlot(gui);
        if (outputSlot == null || this.isValid() == false) return;

        this.clearInputs(gui);
        this.fillInputs(gui, true, outputSlot);

        if (outputSlot.hasStack())
        {
            this.craftAsManyAsPossible(gui);
        }
    }

    public void throwResults(HandledScreen<? extends ScreenHandler> gui)
    {
        Slot outputSlot = this.getOutputSlot(gui);
        if (outputSlot != null && this.isValid())
        {
            InventoryUtils.dropStacks(gui, this.getResult(), outputSlot, false);
        }
    }

}
