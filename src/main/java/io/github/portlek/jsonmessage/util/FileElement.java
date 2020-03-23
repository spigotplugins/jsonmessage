package io.github.portlek.jsonmessage.util;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import io.github.portlek.configs.BukkitManaged;
import io.github.portlek.configs.Managed;
import io.github.portlek.configs.Provided;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public final class FileElement {

    @NotNull
    private final ItemStack itemStack;

    private final int row;

    private final int column;

    @NotNull
    private final String colorCode;

    @NotNull
    private final String formatCode;

    public FileElement(@NotNull final ItemStack itemStack, final int row, final int column) {
        this(itemStack, row, column, "", "");
    }

    public FileElement(@NotNull final ItemStack itemStack, final int row, final int column,
                       @NotNull final String colorCode, @NotNull final String formatCode) {
        this.itemStack = itemStack;
        this.row = row;
        this.column = column;
        this.colorCode = colorCode;
        this.formatCode = formatCode;
    }

    public FileElement(@NotNull final FileElement fileElement) {
        this(
            fileElement.itemStack,
            fileElement.row,
            fileElement.column,
            fileElement.colorCode,
            fileElement.formatCode
        );
    }

    @NotNull
    public String getColorCode() {
        return this.colorCode;
    }

    @NotNull
    public String getFormatCode() {
        return this.formatCode;
    }

    public void insert(@NotNull final InventoryContents contents, @NotNull final Consumer<InventoryClickEvent> consumer) {
        contents.set(this.row, this.column, ClickableItem.of(this.itemStack, consumer));
    }

    public void fill(@NotNull final InventoryContents contents) {
        this.fill(contents, event -> {
        });
    }

    public void fill(@NotNull final InventoryContents contents, @NotNull final Consumer<InventoryClickEvent> consumer) {
        contents.fill(ClickableItem.of(this.itemStack, consumer));
    }

    @NotNull
    public FileElement replace(final boolean name, final boolean lore, @NotNull final Placeholder... placeholders) {
        return this.replace(name, lore, Arrays.asList(placeholders));
    }

    @NotNull
    public FileElement replace(final boolean displayName, final boolean lore,
                               @NotNull final Iterable<Placeholder> placeholders) {
        final ItemStack clone = this.itemStack.clone();
        final ItemMeta itemMeta = clone.getItemMeta();
        if (itemMeta == null) {
            return this;
        }
        if (displayName && itemMeta.hasDisplayName()) {
            for (final Placeholder placeholder : placeholders) {
                itemMeta.setDisplayName(placeholder.replace(itemMeta.getDisplayName()));
            }
        }
        if (lore && itemMeta.getLore() != null && itemMeta.hasLore()) {
            final List<String> finalLore = new ArrayList<>();
            itemMeta.getLore().forEach(s -> {
                final AtomicReference<String> finalString = new AtomicReference<>(s);
                placeholders.forEach(placeholder ->
                    finalString.set(placeholder.replace(finalString.get()))
                );
                finalLore.add(finalString.get());
            });
            itemMeta.setLore(finalLore);
        }
        clone.setItemMeta(itemMeta);
        return new FileElement(clone, this.row, this.column);
    }

    @NotNull
    public ClickableItem clickableItem(@NotNull final Consumer<InventoryClickEvent> consumer) {
        return ClickableItem.of(this.itemStack, consumer);
    }

    @NotNull
    public FileElement changeItem(@NotNull final ItemStack itemStack) {
        return new FileElement(
            itemStack,
            this.row,
            this.column
        );
    }

    @NotNull
    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public int getRow() {
        return this.row;
    }

    public int getColumn() {
        return this.column;
    }

    public static class Provider implements Provided<FileElement> {

        @Override
        public void set(@NotNull final FileElement fileElement, @NotNull final Managed managed, @NotNull final String s) {
            final BukkitManaged bukkitManaged = (BukkitManaged) managed;
            bukkitManaged.set(s + ".row", fileElement.row);
            bukkitManaged.set(s + ".column", fileElement.column);
            bukkitManaged.setItemStack(s, fileElement.itemStack);
        }

        @NotNull
        @Override
        public Optional<FileElement> get(@NotNull final Managed managed, @NotNull final String s) {
            if (!s.contains("element") || !managed.getSection(s).isPresent()) {
                return Optional.empty();
            }
            final BukkitManaged bukkitManaged = (BukkitManaged) managed;
            return bukkitManaged.getItemStack(s).map(stack ->
                new FileElement(
                    stack,
                    bukkitManaged.getInt(s + ".row"),
                    bukkitManaged.getInt(s + ".column")
                )
            );
        }

    }

}
