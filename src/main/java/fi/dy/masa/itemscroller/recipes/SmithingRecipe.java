package fi.dy.masa.itemscroller.recipes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.SmithingScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.screen.slot.Slot;
import fi.dy.masa.itemscroller.util.InventoryUtils;

public class SmithingRecipe extends AbstractRecipePattern
{
    private ItemStack inputTemplate = InventoryUtils.EMPTY_STACK;
    private ItemStack inputEquipment = InventoryUtils.EMPTY_STACK;
    private ItemStack inputMaterial = InventoryUtils.EMPTY_STACK;

    public SmithingRecipe()
    {
        super();
    }

    @Override
    public AbstractRecipePattern.RecipeType getType()
    {
        return AbstractRecipePattern.RecipeType.SMITHING;
    }

    @Override
    public void clearRecipe()
    {
        this.result = InventoryUtils.EMPTY_STACK;
        this.inputTemplate = InventoryUtils.EMPTY_STACK;
        this.inputEquipment = InventoryUtils.EMPTY_STACK;
        this.inputMaterial = InventoryUtils.EMPTY_STACK;
    }

    @Override
    public boolean isEmpty()
    {
        return this.inputEquipment.isEmpty() && this.result.isEmpty();
    }

    @Override
    public void storeRecipe(Slot slot, HandledScreen<? extends ScreenHandler> gui,
                            boolean clearIfEmpty, boolean fromKeybind, MinecraftClient mc)
    {
        if (!(gui instanceof SmithingScreen) || !(gui.getScreenHandler() instanceof SmithingScreenHandler handler))
        {
            return;
        }

        if (slot.hasStack())
        {
            Slot templateSlot = handler.getSlot(0);
            Slot equipmentSlot = handler.getSlot(1);
            Slot materialSlot = handler.getSlot(2);

            this.inputTemplate = templateSlot.hasStack() ? templateSlot.getStack().copy() : InventoryUtils.EMPTY_STACK;
            this.inputEquipment = equipmentSlot.hasStack() ? equipmentSlot.getStack().copy() : InventoryUtils.EMPTY_STACK;
            this.inputMaterial = materialSlot.hasStack() ? materialSlot.getStack().copy() : InventoryUtils.EMPTY_STACK;
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
        if (gui.getScreenHandler() instanceof SmithingScreenHandler handler)
        {
            return handler.getSlot(3);
        }
        return null;
    }

    @Override
    public void fillInputs(HandledScreen<? extends ScreenHandler> gui, boolean fillStacks, Slot outputSlot)
    {
        if (!(gui.getScreenHandler() instanceof SmithingScreenHandler handler))
        {
            return;
        }

        Slot templateSlot = handler.getSlot(0);
        Slot equipmentSlot = handler.getSlot(1);
        Slot materialSlot = handler.getSlot(2);

        boolean needsTemplate = this.inputTemplate.isEmpty() == false &&
                (!templateSlot.hasStack() || !InventoryUtils.areStacksEqual(templateSlot.getStack(), this.inputTemplate));
        boolean needsEquipment = this.inputEquipment.isEmpty() == false &&
                (!equipmentSlot.hasStack() || !InventoryUtils.areStacksEqual(equipmentSlot.getStack(), this.inputEquipment));
        boolean needsMaterial = this.inputMaterial.isEmpty() == false &&
                (!materialSlot.hasStack() || !InventoryUtils.areStacksEqual(materialSlot.getStack(), this.inputMaterial));

        if (needsTemplate || needsEquipment || needsMaterial)
        {
            InventoryUtils.tryClearCursor(gui);
            MinecraftClient mc = MinecraftClient.getInstance();

            if (needsTemplate)
            {
                InventoryUtils.moveItemsFromInventory(
                    gui, templateSlot.id, mc.player.getInventory(), this.inputTemplate, fillStacks);
            }

            if (needsEquipment)
            {
                InventoryUtils.moveItemsFromInventory(
                    gui, equipmentSlot.id, mc.player.getInventory(), this.inputEquipment, fillStacks);
            }

            if (needsMaterial)
            {
                InventoryUtils.moveItemsFromInventory(
                    gui, materialSlot.id, mc.player.getInventory(), this.inputMaterial, fillStacks);
            }
        }
    }

    @Override
    public void clearInputs(HandledScreen<? extends ScreenHandler> gui)
    {
        if (!(gui.getScreenHandler() instanceof SmithingScreenHandler handler)) return;

        for (int i = 0; i < 3; i++)
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
        if (nbt.contains("InputTemplate", fi.dy.masa.itemscroller.util.Constants.NBT.TAG_COMPOUND))
        {
            this.inputTemplate = ItemStack.fromNbtOrEmpty(registryManager, nbt.getCompound("InputTemplate"));
        }
        if (nbt.contains("InputEquipment", fi.dy.masa.itemscroller.util.Constants.NBT.TAG_COMPOUND))
        {
            this.inputEquipment = ItemStack.fromNbtOrEmpty(registryManager, nbt.getCompound("InputEquipment"));
        }
        if (nbt.contains("InputMaterial", fi.dy.masa.itemscroller.util.Constants.NBT.TAG_COMPOUND))
        {
            this.inputMaterial = ItemStack.fromNbtOrEmpty(registryManager, nbt.getCompound("InputMaterial"));
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

            if (this.inputTemplate.isEmpty() == false)
            {
                nbt.put("InputTemplate", this.inputTemplate.encode(registryManager));
            }

            if (this.inputEquipment.isEmpty() == false)
            {
                nbt.put("InputEquipment", this.inputEquipment.encode(registryManager));
            }

            if (this.inputMaterial.isEmpty() == false)
            {
                nbt.put("InputMaterial", this.inputMaterial.encode(registryManager));
            }
        }

        return nbt;
    }
}
