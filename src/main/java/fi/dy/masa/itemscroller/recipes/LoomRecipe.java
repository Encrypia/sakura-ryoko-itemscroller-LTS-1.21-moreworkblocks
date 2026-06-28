package fi.dy.masa.itemscroller.recipes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.LoomScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import fi.dy.masa.itemscroller.util.InventoryUtils;

public class LoomRecipe extends AbstractRecipePattern
{
    private ItemStack inputBanner = InventoryUtils.EMPTY_STACK;
    private ItemStack inputDye = InventoryUtils.EMPTY_STACK;
    private ItemStack inputPattern = InventoryUtils.EMPTY_STACK;
    private String patternId = "";

    public LoomRecipe()
    {
        super();
    }

    @Override
    public AbstractRecipePattern.RecipeType getType()
    {
        return AbstractRecipePattern.RecipeType.LOOM;
    }

    @Override
    public void clearRecipe()
    {
        this.result = InventoryUtils.EMPTY_STACK;
        this.inputBanner = InventoryUtils.EMPTY_STACK;
        this.inputDye = InventoryUtils.EMPTY_STACK;
        this.inputPattern = InventoryUtils.EMPTY_STACK;
        this.patternId = "";
    }

    @Override
    public boolean isEmpty()
    {
        return this.inputBanner.isEmpty() && this.result.isEmpty();
    }

    @Override
    public void storeRecipe(Slot slot, HandledScreen<? extends ScreenHandler> gui,
                            boolean clearIfEmpty, boolean fromKeybind, MinecraftClient mc)
    {
        if (!(gui instanceof LoomScreen) || !(gui.getScreenHandler() instanceof LoomScreenHandler handler))
        {
            return;
        }

        if (slot.hasStack())
        {
            Slot bannerSlot = handler.getBannerSlot();
            Slot dyeSlot = handler.getDyeSlot();
            Slot patternSlot = handler.getPatternSlot();

            this.inputBanner = bannerSlot.hasStack() ? bannerSlot.getStack().copy() : InventoryUtils.EMPTY_STACK;
            this.inputDye = dyeSlot.hasStack() ? dyeSlot.getStack().copy() : InventoryUtils.EMPTY_STACK;
            this.inputPattern = patternSlot.hasStack() ? patternSlot.getStack().copy() : InventoryUtils.EMPTY_STACK;
            this.result = slot.getStack().copy();

            int selected = handler.getSelectedPattern();
            if (selected >= 0 && selected < handler.getBannerPatterns().size())
            {
                RegistryEntry<?> entry = handler.getBannerPatterns().get(selected);
                entry.getKey().ifPresent(key -> this.patternId = key.getValue().toString());
            }
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
        if (gui.getScreenHandler() instanceof LoomScreenHandler handler)
        {
            return handler.getOutputSlot();
        }
        return null;
    }

    @Override
    public void fillInputs(HandledScreen<? extends ScreenHandler> gui, boolean fillStacks, Slot outputSlot)
    {
        if (!(gui.getScreenHandler() instanceof LoomScreenHandler handler) || this.inputBanner.isEmpty())
        {
            return;
        }

        Slot bannerSlot = handler.getBannerSlot();
        Slot dyeSlot = handler.getDyeSlot();
        Slot patternSlot = handler.getPatternSlot();

        boolean needsBanner = !bannerSlot.hasStack() ||
                !InventoryUtils.areStacksEqual(bannerSlot.getStack(), this.inputBanner);
        boolean needsDye = this.inputDye.isEmpty() == false &&
                (!dyeSlot.hasStack() ||
                 !InventoryUtils.areStacksEqual(dyeSlot.getStack(), this.inputDye));
        boolean needsPattern = this.inputPattern.isEmpty() == false &&
                (!patternSlot.hasStack() ||
                 !InventoryUtils.areStacksEqual(patternSlot.getStack(), this.inputPattern));

        if (needsBanner || needsDye || needsPattern)
        {
            InventoryUtils.tryClearCursor(gui);

            MinecraftClient mc = MinecraftClient.getInstance();

            if (needsBanner)
            {
                InventoryUtils.moveItemsFromInventory(
                    gui, bannerSlot.id, mc.player.getInventory(), this.inputBanner, fillStacks);
            }

            if (needsDye)
            {
                InventoryUtils.moveItemsFromInventory(
                    gui, dyeSlot.id, mc.player.getInventory(), this.inputDye, fillStacks);
            }

            if (needsPattern)
            {
                InventoryUtils.moveItemsFromInventory(
                    gui, patternSlot.id, mc.player.getInventory(), this.inputPattern, fillStacks);
            }
        }

        if (!this.patternId.isEmpty())
        {
            int index = findPatternIndex(handler, this.patternId);
            if (index >= 0 && handler.getSelectedPattern() != index)
            {
                MinecraftClient.getInstance().interactionManager.clickButton(handler.syncId, index);
            }
        }
    }

    @Override
    public void clearInputs(HandledScreen<? extends ScreenHandler> gui)
    {
        if (!(gui.getScreenHandler() instanceof LoomScreenHandler handler)) return;

        Slot[] slots = { handler.getBannerSlot(), handler.getDyeSlot(), handler.getPatternSlot() };
        for (Slot slot : slots)
        {
            if (slot.hasStack())
            {
                InventoryUtils.shiftClickSlot(gui, slot.id);
            }
        }
    }

    @Override
    public void craftEverything(HandledScreen<? extends ScreenHandler> gui)
    {
        Slot outputSlot = this.getOutputSlot(gui);
        if (outputSlot == null || this.isValid() == false) return;

        this.fillInputs(gui, true, outputSlot);
        this.craftAsManyAsPossible(gui);
    }

    @Override
    public void readFromNBT(@Nonnull NbtCompound nbt, @Nonnull DynamicRegistryManager registryManager)
    {
        if (nbt.contains("Result", fi.dy.masa.itemscroller.util.Constants.NBT.TAG_COMPOUND))
        {
            this.result = ItemStack.fromNbtOrEmpty(registryManager, nbt.getCompound("Result"));
        }
        if (nbt.contains("InputBanner", fi.dy.masa.itemscroller.util.Constants.NBT.TAG_COMPOUND))
        {
            this.inputBanner = ItemStack.fromNbtOrEmpty(registryManager, nbt.getCompound("InputBanner"));
        }
        if (nbt.contains("InputDye", fi.dy.masa.itemscroller.util.Constants.NBT.TAG_COMPOUND))
        {
            this.inputDye = ItemStack.fromNbtOrEmpty(registryManager, nbt.getCompound("InputDye"));
        }
        if (nbt.contains("InputPattern", fi.dy.masa.itemscroller.util.Constants.NBT.TAG_COMPOUND))
        {
            this.inputPattern = ItemStack.fromNbtOrEmpty(registryManager, nbt.getCompound("InputPattern"));
        }
        if (nbt.contains("PatternId"))
        {
            this.patternId = nbt.getString("PatternId");
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
            nbt.put("InputBanner", this.inputBanner.encode(registryManager));

            if (this.inputDye.isEmpty() == false)
            {
                nbt.put("InputDye", this.inputDye.encode(registryManager));
            }

            if (this.inputPattern.isEmpty() == false)
            {
                nbt.put("InputPattern", this.inputPattern.encode(registryManager));
            }

            if (!this.patternId.isEmpty())
            {
                nbt.putString("PatternId", this.patternId);
            }
        }

        return nbt;
    }

    @Override
    @Nonnull
    public ItemStack[] getInputStacksForDisplay()
    {
        if (this.inputDye.isEmpty() && this.inputPattern.isEmpty())
        {
            return new ItemStack[] { this.inputBanner };
        }
        if (this.inputPattern.isEmpty())
        {
            return new ItemStack[] { this.inputBanner, this.inputDye };
        }
        return new ItemStack[] { this.inputBanner, this.inputDye, this.inputPattern };
    }

    private static int findPatternIndex(LoomScreenHandler handler, String patternId)
    {
        Identifier targetId = Identifier.tryParse(patternId);
        if (targetId == null) return -1;

        for (int i = 0; i < handler.getBannerPatterns().size(); i++)
        {
            RegistryEntry<?> entry = handler.getBannerPatterns().get(i);
            if (entry.getKey().isPresent())
            {
                RegistryKey<?> key = entry.getKey().get();
                if (targetId.equals(key.getValue()))
                {
                    return i;
                }
            }
        }
        return -1;
    }
}
