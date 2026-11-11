
package com.upb.mantenimiento;

import com.upb.mantenimiento.views.HomeView;
import com.upb.mantenimiento.views.EquipmentView;
import com.upb.mantenimiento.views.TechnicianView;
import com.upb.mantenimiento.views.SparePartView;
import com.upb.mantenimiento.views.WorkOrderView;
import com.upb.mantenimiento.views.MaintenanceHistoryView;
import com.upb.mantenimiento.views.ReportsView;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;

public class MainLayout extends AppLayout {

    public MainLayout() {

        // Create the application title
        H1 title = new H1("Equipment Maintenance System");

        title.getStyle()
                .set("font-size", "20px")
                .set("margin", "0");

        // Create the navigation button
        DrawerToggle menuButton = new DrawerToggle();

        addToNavbar(menuButton, title);

        // Create the sidebar
        H2 menuTitle = new H2("Maintenance");
        menuTitle.getStyle().set("margin", "0");

        Paragraph description = new Paragraph(
                "Equipment Management"
        );

        // Create the navigation menu
        SideNav menu = new SideNav();

        menu.addItem(
                new SideNavItem("Home", HomeView.class)
        );

        menu.addItem(
                new SideNavItem("Equipment", EquipmentView.class)
        );

        menu.addItem(
        new SideNavItem("Work Orders", WorkOrderView.class)
);

menu.addItem(
        new SideNavItem("Maintenance History", MaintenanceHistoryView.class)
);

        menu.addItem(
                new SideNavItem("Technicians", TechnicianView.class)
        );

        menu.addItem(
                new SideNavItem("Spare Parts", SparePartView.class)
        );

        menu.addItem(
        new SideNavItem("Reports", ReportsView.class)
);

        // Add the menu to the sidebar
        VerticalLayout sidebar = new VerticalLayout();

        sidebar.add(
                menuTitle,
                description,
                menu
        );

        sidebar.setPadding(true);
        sidebar.setSpacing(true);

        addToDrawer(sidebar);
    }
}

