import { createObstacle, type MapCollision, type Point } from "./collision";
import adventurersGuild from "./maps/city_adv_guild.json";
import forge from "./maps/city_forge.json";
import cityGates from "./maps/city_gates.json";
import merchantsGuild from "./maps/city_merch_guild.json";
import square from "./maps/city_square.json";
import tavern from "./maps/city_tavern.json";
import cryptArmory from "./maps/crypt_armory.json";
import cryptCentral from "./maps/crypt_central.json";
import cryptCorridor from "./maps/crypt_corridor.json";
import cryptEntrance from "./maps/crypt_entrance.json";
import cryptLibrary from "./maps/crypt_library.json";
import cryptThroneRoom from "./maps/crypt_boss_room.json";
import cryptTraining from "./maps/crypt_training.json";
import forestEdge from "./maps/forest_edge.json";
import wolfTrail from "./maps/forest_wolf_trail.json";

interface MapObject extends Point {
    id: number;
    name: string;
    point?: boolean;
    width?: number;
    height?: number;
    ellipse?: boolean;
    polygon?: Point[];
}

interface MapData {
    width: number;
    height: number;
    tilewidth: number;
    tileheight: number;
    layers: Array<{ name: string; image?: string; objects?: MapObject[] }>;
}

export interface WorldMap extends MapCollision {
    ground: string;
    foreground?: string;
    spawn: Point;
    pickups: Array<Point & { id: number }>;
}

const groundWidth = 3072;
const groundHeight = 2048;
const assetRoot = "/assets/game/";

function getOutline(object: MapObject): Point[] {
    const { x, y, width = 0, height = 0, ellipse, polygon } = object;
    if (polygon) return polygon.map((point) => ({ x: x + point.x, y: y + point.y }));
    if (ellipse) {
        const segments = 24;
        return Array.from({ length: segments }, (_, i) => {
            const angle = i * 2 * Math.PI / segments;
            return {
                x: x + width * (1 + Math.cos(angle)) / 2,
                y: y + height * (1 + Math.sin(angle)) / 2,
            };
        });
    }
    return [{ x, y }, { x: x + width, y }, { x: x + width, y: y + height }, { x, y: y + height }];
}

function readMap(data: MapData): WorldMap {
    const width = data.width * data.tilewidth;
    const height = data.height * data.tileheight;
    const ground = data.layers.find((layer) => layer.name === "ground")?.image;
    const foreground = data.layers.find((layer) => layer.name === "foreground")?.image;
    const spawn = data.layers.find((layer) => layer.name === "spawns")?.objects?.find((object) => object.name === "entry");
    const collisions = data.layers.find((layer) => layer.name === "collision")?.objects ?? [];
    const pickups = data.layers.find((layer) => layer.name === "pickups")?.objects ?? [];
    if (!ground || !spawn) throw new Error("У карты отсутствует подложка или точка входа");

    return {
        width,
        height,
        ground: assetRoot + ground.split("/").pop(),
        foreground: foreground ? assetRoot + foreground.split("/").pop() : undefined,
        spawn: { x: spawn.x, y: spawn.y },
        obstacles: collisions.map((object) => createObstacle(getOutline(object))),
        pickups: pickups.filter((object) => object.point).map(({ id, x, y }) => ({ id, x, y })),
    };
}

const annotatedMaps = new Map<string, WorldMap>([
    ["city_adv_guild", readMap(adventurersGuild)],
    ["city_forge", readMap(forge)],
    ["city_gates", readMap(cityGates)],
    ["city_merch_guild", readMap(merchantsGuild)],
    ["city_square", readMap(square)],
    ["city_tavern", readMap(tavern)],
    ["crypt_armory", readMap(cryptArmory)],
    ["crypt_central", readMap(cryptCentral)],
    ["crypt_corridor", readMap(cryptCorridor)],
    ["crypt_entrance", readMap(cryptEntrance)],
    ["crypt_library", readMap(cryptLibrary)],
    ["crypt_boss_room", readMap(cryptThroneRoom)],
    ["crypt_training", readMap(cryptTraining)],
    ["forest_edge", readMap(forestEdge)],
    ["forest_wolf_trail", readMap(wolfTrail)],
]);

const mapArt: Record<string, { ground: string; foreground: string }> = {
    forest_goblin_camp: { ground: "goblin-camp-ground-v2", foreground: "goblin-camp-forest-camp" },
};

export function getWorldMap(locationId: string, fallback: string): WorldMap {
    const annotated = annotatedMaps.get(locationId);
    if (annotated) return annotated;

    const art = Object.prototype.hasOwnProperty.call(mapArt, locationId) ? mapArt[locationId] : undefined;
    return {
        width: groundWidth,
        height: groundHeight,
        ground: art ? `${assetRoot}location-${art.ground}.png` : fallback,
        foreground: art ? `${assetRoot}location-${art.foreground}.png` : undefined,
        spawn: { x: groundWidth / 2, y: groundHeight / 2 },
        obstacles: [],
        pickups: [],
    };
}
