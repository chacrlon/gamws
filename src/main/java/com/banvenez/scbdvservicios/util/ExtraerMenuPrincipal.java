package com.banvenez.scbdvservicios.util;

import com.banvenez.scbdvservicios.dto.MenuDto;

import java.util.ArrayList;
import java.util.List;

public class ExtraerMenuPrincipal {

    public List<MenuDto> extrarPadresMenuPrincipal (List<MenuDto> lstCompleta){
        List<MenuDto> menuPadre = new ArrayList<MenuDto>();
        for (MenuDto  menu  : lstCompleta) {
            if(menu.getMenPadre() == 0){
                menu.setType("sub");
                menu.setIcon("subject");
                menuPadre.add(menu);
            }
        }

        return  menuPadre;
    }

    public List<MenuDto> extrarHijosDelPadreMenuPrincipal(int padre, List<MenuDto> lstCompleta){
        List<MenuDto> menuHijos = new ArrayList<MenuDto>();
        for (MenuDto  menu  : lstCompleta) {
            if(menu.getMenPadre() == padre){
                menu.setType("link");
                menuHijos.add(menu);
            }
        }
        return menuHijos;
    }

}
