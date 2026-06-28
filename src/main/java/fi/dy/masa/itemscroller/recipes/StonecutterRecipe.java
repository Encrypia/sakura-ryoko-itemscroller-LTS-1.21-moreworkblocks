package fi.dy.masa.itemscroller.recipes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.StonecutterScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.screen.slot.Slot;
import fi.dy.masa.itemscroller.util.InventoryUtils;

public class StonecutterRecipe extends AbstractRecipePattern
{
    private ItemStack input = InventoryUtils.EMPTY_STACK;
    private int selectedRecipe = -1;

    public StonecutterRecipe()
    {
        super();
    }

    @Override
    public AbstractRecipePattern.RecipeType getType()
    {
        return AbstractRecipePattern.RecipeType.STONECUTTER;
    }

    @Override
    public void clearRecipe()
    {
        this.result = InventoryUtils.EMPTY_STACK;
        this.input = InventoryUtils.EMPTY_STACK;
        this.selectedRecipe = -1;
    }

    @Override
    public boolean isEmpty()
    {
        return this.input.isEmpty() || this.result.isEmpty();
    }

    @Override
    public void storeRecipe(Slot slot, HandledScreen<? extends ScreenHandler> gui,
                            boolean clearIfEmpty, boolean fromKeybind, MinecraftClient mc)
    {
        if (!(gui instanceof StonecutterScreen) || !(gui.getScreenHandler() instanceof StonecutterScreenHandler handler))
        {
            return;
        }

        if (slot.hasStack())
        {
            Slot inputSlot = handler.getSlot(0);
            if (inputSlot.hasStack())
            {
                this.input = inputSlot.getStack().copy();
            }
            this.result = slot.getStack().copy();
            this.selectedRecipe = handler.getSelectedRecipe();
        }
        else if (clearIfEmpty)
        {
            this.clearRecipe();
        }
    }

    @Override
    @Nullable
    public Slot getOutputSlot(HandledScreen<? extends ScreenHandler> gui)
    {
        if (gui.getScreenHandler() instanceof StonecutterScreenHandler handler)
        {
            return handler.getSlot(1);
        }
        return null;
    }

    @Override
    public boolean canCraft(HandledScreen<? extends ScreenHandler> gui)
    {
        if (!this.isValid()) return false;
        if (!(gui.getScreenHandler() instanceof StonecutterScreenHandler handler)) return false;
        return handler.getSelectedRecipe() == this.selectedRecipe;
    }

    @Override
    public void fillInputs(HandledScreen<? extends ScreenHandler> gui, boolean fillStacks, Slot outputSlot)
    {
        if (!(gui.getScreenHandler() instanceof StonecutterScreenHandler handler) || this.input.isEmpty())
        {
            return;
        }

        Slot inputSlot = handler.getSlot(0);
        MinecraftClient mc = MinecraftClient.getInstance();

        if (inputSlot.hasStack() && InventoryUtils.areStacksEqual(inputSlot.getStack(), this.input) == false)
        {
            InventoryUtils.shiftClickSlot(gui, inputSlot.id);
        }

        if (inputSlot.hasStack() == false || InventoryUtils.areStacksEqual(inputSlot.getStack(), this.input) == false)
        {
            InventoryUtils.tryClearCursor(gui);
            InventoryUtils.moveItemsFromInventory(gui, 0, mc.player.getInventory(), this.input, fillStacks);
        }

        if (this.selectedRecipe >= 0)
        {
            mc.interactionManager.clickButton(handler.syncId, this.selectedRecipe);
        }
    }

    @Override
    public void clearInputs(HandledScreen<? extends ScreenHandler> gui)
    {
        if (!(gui.getScreenHandler() instanceof StonecutterScreenHandler handler)) return;
        Slot inputSlot = handler.getSlot(0);
        if (inputSlot.hasStack())
        {
            InventoryUtils.shiftClickSlot(gui, inputSlot.id);
        }
    }

    // 按住 合成 - 正常合成 按钮批量合成并丢弃产物
    @Override
    public void craftEverything(HandledScreen<? extends ScreenHandler> gui)
    {
        Slot outputSlot = this.getOutputSlot(gui);
        if (outputSlot == null || this.isValid() == false) return;

        this.fillInputs(gui, true, outputSlot);
        this.craftAsManyAsPossible(gui);
    }

    @Override
    public void craftAsManyAsPossible(HandledScreen<? extends ScreenHandler> gui)
    {
        Slot inputSlot = gui.getScreenHandler().getSlot(0);
        Slot outputSlot = this.getOutputSlot(gui);
        if (outputSlot == null || inputSlot == null || this.isValid() == false) return;

        if (inputSlot.hasStack() == false ||
            InventoryUtils.areStacksEqual(inputSlot.getStack(), this.input) == false)
        {
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        StonecutterScreenHandler handler = (StonecutterScreenHandler) gui.getScreenHandler();

        if (this.selectedRecipe >= 0 &&
            handler.getSelectedRecipe() != this.selectedRecipe)
        {
            mc.interactionManager.clickButton(handler.syncId, this.selectedRecipe);
        }

        InventoryUtils.dropStacksWhileHasItem(gui, outputSlot.id, this.getResult());
    }

    @Override
    public void craftAsManyAndKeep(HandledScreen<? extends ScreenHandler> gui)
    {
        Slot inputSlot = gui.getScreenHandler().getSlot(0);
        Slot outputSlot = this.getOutputSlot(gui);
        if (outputSlot == null || inputSlot == null || this.isValid() == false) return;

        if (inputSlot.hasStack() == false ||
            InventoryUtils.areStacksEqual(inputSlot.getStack(), this.input) == false)
        {
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        StonecutterScreenHandler handler = (StonecutterScreenHandler) gui.getScreenHandler();

        if (this.selectedRecipe >= 0 &&
            handler.getSelectedRecipe() != this.selectedRecipe)
        {
            mc.interactionManager.clickButton(handler.syncId, this.selectedRecipe);
        }

        InventoryUtils.shiftClickSlot(gui, outputSlot.id);
    }

    @Override
    public void readFromNBT(@Nonnull NbtCompound nbt, @Nonnull DynamicRegistryManager registryManager)
    {
        if (nbt.contains("Result", fi.dy.masa.itemscroller.util.Constants.NBT.TAG_COMPOUND))
        {
            this.result = ItemStack.fromNbtOrEmpty(registryManager, nbt.getCompound("Result"));
        }
        if (nbt.contains("Input", fi.dy.masa.itemscroller.util.Constants.NBT.TAG_COMPOUND))
        {
            this.input = ItemStack.fromNbtOrEmpty(registryManager, nbt.getCompound("Input"));
        }
        this.selectedRecipe = nbt.getInt("SelectedRecipe");
    }

    @Override
    @Nonnull
    public NbtCompound writeToNBT(@Nonnull DynamicRegistryManager registryManager)
    {
        NbtCompound nbt = new NbtCompound();

        if (this.isValid())
        {
            nbt.put("Result", this.result.encode(registryManager));
            nbt.put("Input", this.input.encode(registryManager));
            nbt.putInt("SelectedRecipe", this.selectedRecipe);
        }

        return nbt;
    }

    public ItemStack getInput()
    {
        return this.input;
    }

    public void setInput(ItemStack input)
    {
        this.input = input;
    }

    public int getSelectedRecipe()
    {
        return this.selectedRecipe;
    }

    @Override
    @Nonnull
    public ItemStack[] getInputStacksForDisplay()
    {
        return new ItemStack[] { this.input };
    }
}
