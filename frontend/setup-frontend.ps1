@"
export default function QuestsPage() {
    return <h1>Quests</h1>;
}
"@ | Set-Content src/pages/QuestsPage.tsx

@"
export default function GamePage() {
    return <h1>Game</h1>;
}
"@ | Set-Content src/pages/GamePage.tsx

@"
export default function CharacterPage() {
    return <h1>Character</h1>;
}
"@ | Set-Content src/pages/CharacterPage.tsx

@"
export default function InventoryPage() {
    return <h1>Inventory</h1>;
}
"@ | Set-Content src/pages/InventoryPage.tsx

@"
export default function ShopPage() {
    return <h1>Shop</h1>;
}
"@ | Set-Content src/pages/ShopPage.tsx

@"
export default function NotFoundPage() {
    return <h1>404 - Page not found</h1>;
}
"@ | Set-Content src/pages/NotFoundPage.tsx