package fi.dy.masa.itemscroller.recipes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GrindstoneScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import fi.dy.masa.itemscroller.util.InventoryUtils;

public class GrindstoneRecipe extends AbstractRecipePattern
{
    private ItemStack inputTop = InventoryUtils.EMPTY_STACK;
    private ItemStack inputBottom = InventoryUtils.EMPTY_STACK;

    public GrindstoneRecipe()
    {
        super();
    }

    @Override
    public AbstractRecipePattern.RecipeType getType()
    {
        return AbstractRecipePattern.RecipeType.GRINDSTONE;
    }

    @Override
    public void clearRecipe()
    {
        this.result = InventoryUtils.EMPTY_STACK;
        this.inputTop = InventoryUtils.EMPTY_STACK;
        this.inputBottom = InventoryUtils.EMPTY_STACK;
    }

    @Override
    public boolean isEmpty()
    {
        return this.inputTop.isEmpty() && this.result.isEmpty();
    }

    @Override
    public void storeRecipe(Slot slot, HandledScreen<? extends ScreenHandler> gui,
                            boolean clearIfEmpty, boolean fromKeybind, MinecraftClient mc)
    {
        if (!(gui instanceof GrindstoneScreen) || !(gui.getScreenHandler() instanceof GrindstoneScreenHandler handler))
        {
            return;
        }

        if (slot.hasStack())
        {
            Slot topSlot = handler.getSlot(0);
            Slot bottomSlot = handler.getSlot(1);

            this.inputTop = topSlot.hasStack() ? topSlot.getStack().copy() : InventoryUtils.EMPTY_STACK;
            this.inputBottom = bottomSlot.hasStack() ? bottomSlot.getStack().copy() : InventoryUtils.EMPTY_STACK;
            this.result = slot.getStack().copy();
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
        if (gui.getScreenHandler() instanceof GrindstoneScreenHandler handler)
        {
            return handler.getSlot(2);
        }
        return null;
    }

    @Override
    public void fillInputs(HandledScreen<? extends ScreenHandler> gui, boolean fillStacks, Slot outputSlot)
    {
        if (!(gui.getScreenHandler() instanceof GrindstoneScreenHandler handler))
        {
            return;
        }

        Slot topSlot = handler.getSlot(0);
        Slot bottomSlot = handler.getSlot(1);

        boolean needsTop = this.inputTop.isEmpty() == false &&
                (!topSlot.hasStack() || !InventoryUtils.areStacksEqual(topSlot.getStack(), this.inputTop));
        boolean needsBottom = this.inputBottom.isEmpty() == false &&
                (!bottomSlot.hasStack() || !InventoryUtils.areStacksEqual(bottomSlot.getStack(), this.inputBottom));

        if (needsTop || needsBottom)
        {
            InventoryUtils.tryClearCursor(gui);

            if (needsTop)
            {
                InventoryUtils.moveItemsFromInventory(
                    gui, 0, MinecraftClient.getInstance().player.getInventory(), this.inputTop, fillStacks);
            }

            if (needsBottom)
            {
                InventoryUtils.moveItemsFromInventory(
                    gui, 1, MinecraftClient.getInstance().player.getInventory(), this.inputBottom, fillStacks);
            }
        }
    }

    @Override
    public void clearInputs(HandledScreen<? extends ScreenHandler> gui)
    {
        if (!(gui.getScreenHandler() instanceof GrindstoneScreenHandler handler)) return;

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
        if (nbt.contains("InputTop", fi.dy.masa.itemscroller.util.Constants.NBT.TAG_COMPOUND))
        {
            this.inputTop = ItemStack.fromNbtOrEmpty(registryManager, nbt.getCompound("InputTop"));
        }
        if (nbt.contains("InputBottom", fi.dy.masa.itemscroller.util.Constants.NBT.TAG_COMPOUND))
        {
            this.inputBottom = ItemStack.fromNbtOrEmpty(registryManager, nbt.getCompound("InputBottom"));
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
            nbt.put("InputTop", this.inputTop.encode(registryManager));

            if (this.inputBottom.isEmpty() == false)
            {
                nbt.put("InputBottom", this.inputBottom.encode(registryManager));
            }
        }

        return nbt;
    }

    public ItemStack getInputTop()
    {
        return this.inputTop;
    }

    public ItemStack getInputBottom()
    {
        return this.inputBottom;
    }
}
