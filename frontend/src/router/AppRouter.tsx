import { BrowserRouter, Routes, Route } from "react-router-dom";

import LoginPage from "../pages/LoginPage";
import RegisterPage from "../pages/RegisterPage";
import GamePage from "../pages/GamePage";
import CharacterPage from "../pages/CharacterPage";
import InventoryPage from "../pages/InventoryPage";
import QuestsPage from "../pages/QuestsPage";
import ShopPage from "../pages/ShopPage";
import NotFoundPage from "../pages/NotFoundPage";

import ProtectedRoute from "./ProtectedRoute";

export default function AppRouter() {

    return (
        <BrowserRouter>

            <Routes>

                <Route
                    path="/login"
                    element={<LoginPage />}
                />

                <Route
                    path="/register"
                    element={<RegisterPage />}
                />

                <Route element={<ProtectedRoute />}>

                    <Route
                        path="/"
                        element={<GamePage />}
                    />

                    <Route
                        path="/character"
                        element={<CharacterPage />}
                    />

                    <Route
                        path="/inventory"
                        element={<InventoryPage />}
                    />

                    <Route
                        path="/quests"
                        element={<QuestsPage />}
                    />

                    <Route
                        path="/shop"
                        element={<ShopPage />}
                    />

                </Route>

                <Route
                    path="*"
                    element={<NotFoundPage />}
                />

            </Routes>

        </BrowserRouter>
    );
}