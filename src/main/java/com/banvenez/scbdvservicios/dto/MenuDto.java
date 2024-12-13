package com.banvenez.scbdvservicios.dto;

import java.util.List;

public class MenuDto {

    private String route;
    private String name;
    private String type;
    private String icon;
    private Integer menPadre;
    private Integer codMenu;
    private List<MenuDto> children;
    private Integer ordMenu;


    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<MenuDto> getChildren() {
        return children;
    }

    public void setChildren(List<MenuDto> children) {
        this.children = children;
    }

    public Integer getMenPadre() {
        return menPadre;
    }

    public void setMenPadre(Integer menPadre) {
        this.menPadre = menPadre;
    }

    public Integer getCodMenu() {
        return codMenu;
    }

    public void setCodMenu(Integer codMenu) {
        this.codMenu = codMenu;
    }



    public Integer getOrdMenu() {
        return ordMenu;
    }

    public void setOrdMenu(Integer ordMenu) {
        this.ordMenu = ordMenu;
    }



}
