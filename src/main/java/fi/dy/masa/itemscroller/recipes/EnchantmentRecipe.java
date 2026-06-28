package fi.dy.masa.itemscroller.recipes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import fi.dy.masa.itemscroller.util.InventoryUtils;

public class EnchantmentRecipe extends AbstractRecipePattern
{
    private ItemStack inputStack = InventoryUtils.EMPTY_STACK;
    private int enchantmentOption = -1;

    public EnchantmentRecipe()
    {
        super();
    }

    @Override
    public AbstractRecipePattern.RecipeType getType()
    {
        return AbstractRecipePattern.RecipeType.ENCHANTMENT;
    }

    @Override
    public void clearRecipe()
    {
        this.result = InventoryUtils.EMPTY_STACK;
        this.inputStack = InventoryUtils.EMPTY_STACK;
        this.enchantmentOption = -1;
    }

    @Override
    public boolean isEmpty()
    {
        return this.inputStack.isEmpty();
    }

    @Override
    public void storeRecipe(Slot slot, HandledScreen<? extends ScreenHandler> gui,
                            boolean clearIfEmpty, boolean fromKeybind, MinecraftClient mc)
    {
        if (!(gui instanceof EnchantmentScreen) || !(gui.getScreenHandler() instanceof EnchantmentScreenHandler handler))
        {
            return;
        }

        Slot inputSlot = handler.getSlot(0);

        if (inputSlot.hasStack())
        {
            this.inputStack = inputSlot.getStack().copy();
            this.result = this.inputStack.copy();
            this.enchantmentOption = 0;
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
        if (gui.getScreenHandler() instanceof EnchantmentScreenHandler handler)
        {
            return handler.getSlot(0);
        }
        return null;
    }

    @Override
    public boolean canCraft(HandledScreen<? extends ScreenHandler> gui)
    {
        if (!this.isValid()) return false;
        if (!(gui.getScreenHandler() instanceof EnchantmentScreenHandler handler)) return false;

        Slot inputSlot = handler.getSlot(0);
        Slot lapisSlot = handler.getSlot(1);

        return inputSlot.hasStack() &&
               inputSlot.getStack().getItem() == this.inputStack.getItem() &&
               !inputSlot.getStack().hasEnchantments() &&
               lapisSlot.hasStack() &&
               lapisSlot.getStack().isOf(Items.LAPIS_LAZULI);
    }

    @Override
    public void fillInputs(HandledScreen<? extends ScreenHandler> gui, boolean fillStacks, Slot outputSlot)
    {
        if (!(gui.getScreenHandler() instanceof EnchantmentScreenHandler handler) || this.inputStack.isEmpty())
        {
            return;
        }

        Slot inputSlot = handler.getSlot(0);
        Slot lapisSlot = handler.getSlot(1);
        MinecraftClient mc = MinecraftClient.getInstance();

        if (inputSlot.hasStack())
        {
            if (inputSlot.getStack().hasEnchantments())
            {
            InventoryUtils.shiftClickSlot(gui, inputSlot.id);
            }
        }

        if (!inputSlot.hasStack() ||
            inputSlot.getStack().getItem() != this.inputStack.getItem() ||
            inputSlot.getStack().hasEnchantments())
        {
            InventoryUtils.tryClearCursor(gui);
            InventoryUtils.moveItemsFromInventory(
                gui, inputSlot.id, mc.player.getInventory(), this.inputStack, fillStacks);
        }

        Slot currentLapisSlot = handler.getSlot(1);
        if (!currentLapisSlot.hasStack() || !currentLapisSlot.getStack().isOf(Items.LAPIS_LAZULI))
        {
            ItemStack lapisStack = new ItemStack(Items.LAPIS_LAZULI);
            InventoryUtils.tryClearCursor(gui);
            InventoryUtils.moveItemsFromInventory(
                gui, lapisSlot.id, mc.player.getInventory(), lapisStack, true);
        }
    }

    @Override
    public void clearInputs(HandledScreen<? extends ScreenHandler> gui)
    {
        if (!(gui.getScreenHandler() instanceof EnchantmentScreenHandler handler)) return;

        Slot inputSlot = handler.getSlot(0);
        if (inputSlot.hasStack())
        {
            InventoryUtils.shiftClickSlot(gui, inputSlot.id);
        }
    }

    @Override
    public void craftEverything(HandledScreen<? extends ScreenHandler> gui)
    {
        if (!(gui.getScreenHandler() instanceof EnchantmentScreenHandler handler)) return;

        Slot inputSlot = handler.getSlot(0);

        if (inputSlot.hasStack() && inputSlot.getStack().hasEnchantments())
        {
            InventoryUtils.shiftClickSlot(gui, inputSlot.id);
        }

        this.fillInputs(gui, true, inputSlot);
    }

    @Override
    public void craftAsManyAsPossible(HandledScreen<? extends ScreenHandler> gui)
    {
        if (!(gui.getScreenHandler() instanceof EnchantmentScreenHandler handler)) return;

        Slot inputSlot = handler.getSlot(0);
        Slot lapisSlot = handler.getSlot(1);
        MinecraftClient mc = MinecraftClient.getInstance();

        if (!inputSlot.hasStack())
        {
            return;
        }

        if (inputSlot.getStack().hasEnchantments())
        {
            InventoryUtils.shiftClickSlot(gui, inputSlot.id);
            return;
        }

        if (inputSlot.getStack().getItem() == this.inputStack.getItem() &&
            lapisSlot.hasStack() && lapisSlot.getStack().isOf(Items.LAPIS_LAZULI) &&
            this.enchantmentOption >= 0 && this.enchantmentOption <= 2)
        {
            int[] powers = handler.enchantmentPower;
            if (this.enchantmentOption < powers.length && powers[this.enchantmentOption] > 0)
            {
                mc.interactionManager.clickButton(handler.syncId, this.enchantmentOption);
            }
        }
    }

    @Override
    public void craftAsManyAndDrop(HandledScreen<? extends ScreenHandler> gui)
    {
        if (!(gui.getScreenHandler() instanceof EnchantmentScreenHandler handler)) return;

        Slot inputSlot = handler.getSlot(0);
        Slot lapisSlot = handler.getSlot(1);
        MinecraftClient mc = MinecraftClient.getInstance();

        if (!inputSlot.hasStack())
        {
            return;
        }

        if (inputSlot.getStack().hasEnchantments())
        {
            InventoryUtils.dropStacksWhileHasItem(gui, inputSlot.id, inputSlot.getStack());
            return;
        }

        if (inputSlot.getStack().getItem() == this.inputStack.getItem() &&
            lapisSlot.hasStack() && lapisSlot.getStack().isOf(Items.LAPIS_LAZULI) &&
            this.enchantmentOption >= 0 && this.enchantmentOption <= 2)
        {
            int[] powers = handler.enchantmentPower;
            if (this.enchantmentOption < powers.length && powers[this.enchantmentOption] > 0)
            {
                mc.interactionManager.clickButton(handler.syncId, this.enchantmentOption);
            }
        }
    }

    @Override
    public void readFromNBT(@Nonnull NbtCompound nbt, @Nonnull DynamicRegistryManager registryManager)
    {
        if (nbt.contains("Input", fi.dy.masa.itemscroller.util.Constants.NBT.TAG_COMPOUND))
        {
            this.inputStack = ItemStack.fromNbtOrEmpty(registryManager, nbt.getCompound("Input"));
        }
        this.enchantmentOption = nbt.getInt("EnchantmentOption");
    }

    @Override
    @Nonnull
    public NbtCompound writeToNBT(@Nonnull DynamicRegistryManager registryManager)
    {
        NbtCompound nbt = new NbtCompound();

        if (!this.inputStack.isEmpty())
        {
            nbt.put("Input", this.inputStack.encode(registryManager));
            nbt.putInt("EnchantmentOption", this.enchantmentOption);
        }

        return nbt;
    }

    public ItemStack getInputStack()
    {
        return this.inputStack;
    }

    public int getEnchantmentOption()
    {
        return this.enchantmentOption;
    }

    public void setEnchantmentOption(int option)
    {
        this.enchantmentOption = option;
    }
}
