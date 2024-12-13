package com.banvenez.scbdvservicios.dto;

import java.util.List;

public class JsonEnvioMenuDto {

    private List<MenuDto> menu;

    public JsonEnvioMenuDto(List<MenuDto> menu) {
        this.menu = menu;
    }

    public List<MenuDto> getMenu() {
        return menu;
    }

    public void setMenu(List<MenuDto> menu) {
        this.menu = menu;
    }
}
