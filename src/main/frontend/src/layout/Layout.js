import React from "react";
import {useNavigate} from "react-router";
import {useDispatch, useSelector} from "react-redux";
import {toggleDrawer} from "./redux";
import {TopBar} from "./TopBar";
import {MainDrawer} from "./MainDrawer";
import {Box, Toolbar} from "@mui/material";

export const Layout = ({children}) => {
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const drawerOpen = useSelector((state) => state.layout.drawerOpen);
    const doToggleDrawer = () => dispatch(toggleDrawer());
    return (
        <Box
            sx={{
                display: "flex",
            }}
        >
            <TopBar
                goHome={() => navigate("/")}
                newTask={() => {
                    /* TODO */
                }}
                toggleDrawer={doToggleDrawer}
                drawerOpen={drawerOpen}
            />
            <MainDrawer toggleDrawer={doToggleDrawer} drawerOpen={drawerOpen}/>
            <Box
                sx={{
                    flex: 1,
                }}
            >
                <Toolbar/>
                <Box component={"main"}>{children}</Box>
            </Box>
        </Box>
    );
};
