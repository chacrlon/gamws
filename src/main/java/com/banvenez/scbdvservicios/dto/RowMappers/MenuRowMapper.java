package com.banvenez.scbdvservicios.dto.RowMappers;

import com.banvenez.scbdvservicios.dto.MenuDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MenuRowMapper implements RowMapper<MenuDto> {
    @Override
    public MenuDto mapRow(ResultSet rs, int i) throws SQLException {
        MenuDto menu = new MenuDto();
            menu.setCodMenu(rs.getInt("COD_MENU"));
            menu.setName(rs.getString("DES_MENU"));
            menu.setRoute(rs.getString("DIR_MENU").toUpperCase().equals("SIN DIRECCION") ? "": rs.getString("DIR_MENU"));
            menu.setMenPadre(rs.getInt("MEN_PADRE"));
            menu.setOrdMenu(rs.getInt("ORD_MENU"));
        return menu;
    }
}
