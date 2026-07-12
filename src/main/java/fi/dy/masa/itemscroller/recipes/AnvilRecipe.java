package fi.dy.masa.itemscroller.recipes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import fi.dy.masa.itemscroller.util.InventoryUtils;
import fi.dy.masa.itemscroller.mixin.screen.IMixinAnvilScreen;

public class AnvilRecipe extends AbstractRecipePattern
{
    private ItemStack inputLeft = InventoryUtils.EMPTY_STACK;
    private ItemStack inputRight = InventoryUtils.EMPTY_STACK;
    private String renameText = "";
    private boolean hasRename = false;

    public AnvilRecipe()
    {
        super();
    }

    @Override
    public AbstractRecipePattern.RecipeType getType()
    {
        return AbstractRecipePattern.RecipeType.ANVIL;
    }

    @Override
    public void clearRecipe()
    {
        this.result = InventoryUtils.EMPTY_STACK;
        this.inputLeft = InventoryUtils.EMPTY_STACK;
        this.inputRight = InventoryUtils.EMPTY_STACK;
        this.renameText = "";
        this.hasRename = false;
    }

    @Override
    public boolean isEmpty()
    {
        return this.inputLeft.isEmpty() && this.result.isEmpty();
    }

    @Override
    public void storeRecipe(Slot slot, HandledScreen<? extends ScreenHandler> gui,
                            boolean clearIfEmpty, boolean fromKeybind, MinecraftClient mc)
    {
        if (!(gui instanceof AnvilScreen anvilScreen) || !(gui.getScreenHandler() instanceof AnvilScreenHandler handler))
        {
            return;
        }

        if (slot.hasStack())
        {
            Slot leftSlot = handler.getSlot(0);
            Slot rightSlot = handler.getSlot(1);

            this.inputLeft = leftSlot.hasStack() ? leftSlot.getStack().copy() : InventoryUtils.EMPTY_STACK;
            this.inputRight = rightSlot.hasStack() ? rightSlot.getStack().copy() : InventoryUtils.EMPTY_STACK;
            this.result = slot.getStack().copy();

            TextFieldWidget nameField = ((IMixinAnvilScreen) anvilScreen).itemscroller_getNameField();
            this.renameText = nameField != null ? nameField.getText() : "";
            this.hasRename = !this.renameText.isEmpty();
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
        if (gui.getScreenHandler() instanceof AnvilScreenHandler handler)
        {
            return handler.getSlot(2);
        }
        return null;
    }

    @Override
    public void fillInputs(HandledScreen<? extends ScreenHandler> gui, boolean fillStacks, Slot outputSlot)
    {
        if (!(gui.getScreenHandler() instanceof AnvilScreenHandler handler) || this.inputLeft.isEmpty())
        {
            return;
        }

        Slot leftSlot = handler.getSlot(0);
        Slot rightSlot = handler.getSlot(1);

        boolean needsLeft = !leftSlot.hasStack() ||
                !InventoryUtils.areStacksEqual(leftSlot.getStack(), this.inputLeft);
        boolean needsRight = this.inputRight.isEmpty() == false &&
                (!rightSlot.hasStack() ||
                 !InventoryUtils.areStacksEqual(rightSlot.getStack(), this.inputRight));

        if (needsLeft || needsRight)
        {
            InventoryUtils.tryClearCursor(gui);

            if (needsLeft)
            {
                InventoryUtils.moveItemsFromInventory(
                    gui, 0, MinecraftClient.getInstance().player.getInventory(), this.inputLeft, fillStacks);
            }

            if (needsRight)
            {
                InventoryUtils.moveItemsFromInventory(
                    gui, 1, MinecraftClient.getInstance().player.getInventory(), this.inputRight, fillStacks);
            }
        }

        if (this.hasRename && gui instanceof AnvilScreen anvilScreen)
        {
            if (leftSlot.hasStack() == false)
            {
                return;
            }
            if (this.inputRight.isEmpty() == false && rightSlot.hasStack() == false)
            {
                return;
            }
            TextFieldWidget nameField = ((IMixinAnvilScreen) anvilScreen).itemscroller_getNameField();
            String currentName = nameField.getText();
            if (!this.renameText.equals(currentName))
            {
                nameField.setText(this.renameText);
            }
        }
    }

    @Override
    public void clearInputs(HandledScreen<? extends ScreenHandler> gui)
    {
        if (!(gui.getScreenHandler() instanceof AnvilScreenHandler handler)) return;

        for (int i = 0; i < 2; i++)
        {
            Slot inputSlot = handler.getSlot(i);
            if (inputSlot.hasStack())
            {
                InventoryUtils.shiftClickSlot(gui, inputSlot.id);
            }
        }
    }

    @Override
    public void readFromNBT(@Nonnull NbtCompound nbt, @Nonnull DynamicRegistryManager registryManager)
    {
        if (nbt.contains("Result", fi.dy.masa.itemscroller.util.Constants.NBT.TAG_COMPOUND))
        {
            this.result = ItemStack.fromNbtOrEmpty(registryManager, nbt.getCompound("Result"));
        }
        if (nbt.contains("InputLeft", fi.dy.masa.itemscroller.util.Constants.NBT.TAG_COMPOUND))
        {
            this.inputLeft = ItemStack.fromNbtOrEmpty(registryManager, nbt.getCompound("InputLeft"));
        }
        if (nbt.contains("InputRight", fi.dy.masa.itemscroller.util.Constants.NBT.TAG_COMPOUND))
        {
            this.inputRight = ItemStack.fromNbtOrEmpty(registryManager, nbt.getCompound("InputRight"));
        }
        if (nbt.contains("RenameText"))
        {
            this.renameText = nbt.getString("RenameText");
            this.hasRename = !this.renameText.isEmpty();
        }
    }

    @Override
    @Nonnull
    public NbtCompound writeToNBT(@Nonnull DynamicRegistryManager registryManager)
    {
        NbtCompound nbt = new NbtCompound();

        if (this.isValid())
        {
            nbt.put("Result", this.result.encode(registryManager));
            nbt.put("InputLeft", this.inputLeft.encode(registryManager));

            if (this.inputRight.isEmpty() == false)
            {
                nbt.put("InputRight", this.inputRight.encode(registryManager));
            }

            if (this.hasRename)
            {
                nbt.putString("RenameText", this.renameText);
            }
        }

        return nbt;
    }

    public ItemStack getInputLeft()
    {
        return this.inputLeft;
    }

    public ItemStack getInputRight()
    {
        return this.inputRight;
    }

    public String getRenameText()
    {
        return this.renameText;
    }

    public boolean hasRename()
    {
        return this.hasRename;
    }

    @Override
    @Nonnull
    public ItemStack[] getInputStacksForDisplay()
    {
        if (this.inputRight.isEmpty())
        {
            return new ItemStack[] { this.inputLeft };
        }
        return new ItemStack[] { this.inputLeft, this.inputRight };
    }

    @Override
    @Nullable
    public String getDisplayText()
    {
        return this.hasRename ? this.renameText : null;
    }
}
