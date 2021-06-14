package de.bossascrew.pathfinder.util;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.ArrayList;
import java.util.List;

public class PagedChatMenu {

    private final Component title;
    private final int entriesPerPage;
    private final List<Component> entries;
    @Getter
    private int pageCount = 1;
    private final String command;

    private static final String color = "#FF8000";
    private static final String colorDarkened = "#5a2d00";

    /**
     * @param title          Titel des Menüs. Header-Balken müssen im Title enthalten sein, macht das Menü nicht automatisch
     * @param entriesPerPage Einträge pro Seite
     * @param command        wird ausgeführt wenn nächste oder vorherige Seite aufgerufen werden. %PAGE% wird durch die Seitennummer ersetzt
     */
    public PagedChatMenu(Component title, int entriesPerPage, String command) {
        this.title = title;
        this.entriesPerPage = entriesPerPage;
        this.entries = new ArrayList<>();
        this.command = command;
    }

    public void addEntry(Component entry) {
        entries.add(entry);
        pageCount = calcPageCount();
    }

    public Component getPage(int pageNumber) {
        int startIndex = entriesPerPage * pageNumber - 1;
        int stopIndex = startIndex + entriesPerPage;
        stopIndex = stopIndex >= entries.size() ? entries.size() - 1 : stopIndex;
        List<Component> pageElements = entries.subList(startIndex, stopIndex);
        Component page = Component.join(Component.text("\n » ", NamedTextColor.WHITE), pageElements);
        return title.asComponent().append(page).append(getBottomLine(pageNumber));
    }

    private int calcPageCount() {
        int ret = 1;
        double pages = ((double) entries.size()) / entriesPerPage;
        if (pages != (int) pages) {
            ret = (int) pages + 1;
        }
        return ret;
    }

    private Component getBottomLine(int pageNumber) {
        return Component.text("\n")
                .append(getPrevSite(pageNumber))
                .append(Component.text("   "))
                .append(getNextSite(pageNumber));
    }

    private Component getNextSite(int page) {
        boolean isClickable = page == calcPageCount() - 1;
        String hex = isClickable ? color : colorDarkened;
        Component button = getBoxedComponent(Component.text("Weiter", TextColor.fromHexString(hex)));
        if (isClickable) {
            return button;
        }

        button.hoverEvent(HoverEvent.showText(Component.text("Klicke, um die nächste Seite zu öffnen")))
                .clickEvent(ClickEvent.runCommand(command.replace("%PAGE%", page + "")));
        return button;
    }

    private Component getPrevSite(int page) {
        boolean isClickable = page > 0;
        String hex = isClickable ? color : colorDarkened;
        Component button = getBoxedComponent(Component.text("Zurück", TextColor.fromHexString(hex)));
        if (!isClickable) {
            return button;
        }

        button.hoverEvent(HoverEvent.showText(Component.text("Klicke, um die vorherige Seite zu öffnen")))
                .clickEvent(ClickEvent.runCommand(command.replace("%PAGE%", page + "")));
        return button;
    }

    private Component getBoxedComponent(Component component) {
        return Component.text("[", NamedTextColor.GRAY)
                .append(component)
                .append(Component.text("]", NamedTextColor.GRAY));
    }
}
