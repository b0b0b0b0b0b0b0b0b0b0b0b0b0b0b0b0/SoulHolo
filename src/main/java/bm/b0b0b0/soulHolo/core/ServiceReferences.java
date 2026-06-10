package bm.b0b0b0.soulHolo.core;

import bm.b0b0b0.soulHolo.service.GuiNavigationService;

public final class ServiceReferences {

    private volatile GuiNavigationService guiNavigation;

    public GuiNavigationService guiNavigation() {
        return guiNavigation;
    }

    public void setGuiNavigation(GuiNavigationService guiNavigation) {
        this.guiNavigation = guiNavigation;
    }
}
