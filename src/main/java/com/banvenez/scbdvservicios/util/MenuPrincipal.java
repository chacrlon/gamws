package com.banvenez.scbdvservicios.util;

import com.banvenez.scbdvservicios.dto.MenuDto;

import java.util.ArrayList;
import java.util.List;

//public class MenuPrincipal {
//    MsintDao daoMsint =  new MsintDao();
//
//    public List<MenuDto> cargar(String cedula, String app){
//        List<MenuDto> menuGeneral = new ArrayList<MenuDto>();
//        List<MenuDto> menuGeneral2 = new ArrayList<MenuDto>();
//
//        try {
//            List<MenuDto> menuPadre = new ArrayList<MenuDto>();
//            List<MenuDto> lsthijo = new ArrayList<MenuDto>();
//            ExtraerMenuPrincipal extraer = new ExtraerMenuPrincipal();
//
//            // buscar menu a MSINT
//            List<MenuDto> menuCompleto = daoMsint.menuAppUsuario(cedula, app);
//
//            // Buscando menu padres
//            menuPadre = extraer.extrarPadresMenuPrincipal(menuCompleto);
//
//            for (MenuDto padre: menuPadre) {
//                int padreM =  padre.getCodMenu();
//                //BUSCAR HIJOS NIVEL 1
//                lsthijo = extraer.extrarHijosDelPadreMenuPrincipal(padreM, menuCompleto);
//                padre.setType("sub");
//                padre.setIcon("subject");
//                padre.setChildren(lsthijo);
//                menuGeneral.add(padre);
//                for(MenuDto menuHijo1 : lsthijo){
//                    //BUSCAR HIJOS NIVEL 2
//                    padreM =  menuHijo1.getCodMenu();
//                    lsthijo = extraer.extrarHijosDelPadreMenuPrincipal(padreM, menuCompleto);
//                    if(lsthijo.size() > 0){
//                        menuHijo1.setType("sub");
//                        menuHijo1.setIcon("subject");
//                        menuHijo1.setChildren(lsthijo);
//                        padre = (menuHijo1);
//                        //menuGeneral2.add(padre);
//                        //menuGeneral.addAll(menuGeneral2);
//                    }
//
//                    for (MenuDto menuHijo2 : lsthijo){
//                        //BUSCAR HIJOS NIVEL 3
//                        padreM =  menuHijo2.getCodMenu();
//                        lsthijo = extraer.extrarHijosDelPadreMenuPrincipal(padreM, menuCompleto);
//                        if(lsthijo.size() > 0){
//                            menuHijo2.setType("sub");
//                            menuHijo2.setIcon("subject");
//                            menuHijo2.setChildren(lsthijo);
//                            menuHijo1 = menuHijo2;
//                        }
////                        menuGeneral2 = new ArrayList<MenuDto>();
////                        menuGeneral2.add(menuHijo1);
//                      //  menuGeneral.addAll(menuGeneral2);
//                        for(MenuDto menuHijo3 : lsthijo){
//                            //BUSCAR HIJOS NIVEL 4
//                            padreM =  menuHijo3.getCodMenu();
//                            lsthijo = extraer.extrarHijosDelPadreMenuPrincipal(padreM, menuCompleto);
//                            if(lsthijo.size() > 0){
//                                menuHijo3.setType("sub");
//                                menuHijo3.setIcon("subject");
//                                menuHijo3.setChildren(lsthijo);
//                                menuHijo2 = menuHijo3;
//                            }
//
////                            menuGeneral2 = new ArrayList<MenuDto>();
////                            menuGeneral2.add(menuHijo2);
////                            menuGeneral.addAll(menuGeneral2);
//                            for(MenuDto menuHijo4 : lsthijo){
//                                //BUSCAR HIJOS NIVEL 5
//                                padreM =  menuHijo4.getCodMenu();
//                                lsthijo = extraer.extrarHijosDelPadreMenuPrincipal(padreM, menuCompleto);
//                                if(lsthijo.size() > 0){
//                                    menuHijo4.setType("sub");
//                                    menuHijo4.setIcon("subject");
//                                    menuHijo4.setChildren(lsthijo);
//                                    menuHijo3 = menuHijo4;
//                                }
//
////                                menuGeneral2 = new ArrayList<MenuDto>();
////                                menuGeneral2.add(menuHijo3);
////                                menuGeneral.addAll(menuGeneral2);
//                            }
//                        }
//                    }
//
//                }
//
//
//            }
//
//        }catch (Exception e){
//            System.out.println("Error recorriendo Menu " + e.getMessage());
//        }
//
//        return menuGeneral;
//    }
//}
