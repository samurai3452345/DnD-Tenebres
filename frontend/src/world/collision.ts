export interface Point {
    x: number;
    y: number;
}

export interface Obstacle {
    points: Point[];
    left: number;
    right: number;
    top: number;
    bottom: number;
}

export interface MapCollision {
    width: number;
    height: number;
    obstacles: Obstacle[];
}

export const HERO_RADIUS = 21;

export function createObstacle(points: Point[]): Obstacle {
    const xs = points.map(({ x }) => x);
    const ys = points.map(({ y }) => y);
    return {
        points,
        left: Math.min(...xs),
        right: Math.max(...xs),
        top: Math.min(...ys),
        bottom: Math.max(...ys),
    };
}

function touchesObstacle(position: Point, obstacle: Obstacle): boolean {
    const { x, y } = position;
    const { left, right, top, bottom, points } = obstacle;
    if (x < left - HERO_RADIUS || x > right + HERO_RADIUS ||
        y < top - HERO_RADIUS || y > bottom + HERO_RADIUS) {
        return false;
    }

    let inside = false;
    for (let i = 0, j = points.length - 1; i < points.length; j = i++) {
        const a = points[j];
        const b = points[i];
        const dx = b.x - a.x;
        const dy = b.y - a.y;
        const lengthSquared = dx * dx + dy * dy;
        if (lengthSquared === 0) continue;

        const projection = ((x - a.x) * dx + (y - a.y) * dy) / lengthSquared;
        const closest = Math.max(0, Math.min(1, projection));
        const offsetX = x - (a.x + closest * dx);
        const offsetY = y - (a.y + closest * dy);
        if (offsetX * offsetX + offsetY * offsetY < HERO_RADIUS * HERO_RADIUS) return true;

        if ((a.y > y) !== (b.y > y) && x < a.x + (y - a.y) * dx / dy) {
            inside = !inside;
        }
    }
    return inside;
}

export function canStandAt(position: Point, map: MapCollision): boolean {
    const { x, y } = position;
    return x >= HERO_RADIUS && x <= map.width - HERO_RADIUS &&
        y >= HERO_RADIUS && y <= map.height - HERO_RADIUS &&
        !map.obstacles.some((obstacle) => touchesObstacle(position, obstacle));
}

export function moveHero(position: Point, offset: Point, map: MapCollision): Point {
    // Short steps keep the hero from jumping through narrow obstacles after a slow frame.
    const steps = Math.max(1, Math.ceil(Math.hypot(offset.x, offset.y) / (HERO_RADIUS / 2)));
    const stepX = offset.x / steps;
    const stepY = offset.y / steps;
    let { x, y } = position;

    for (let step = 0; step < steps; step++) {
        const nextX = Math.max(HERO_RADIUS, Math.min(map.width - HERO_RADIUS, x + stepX));
        if (canStandAt({ x: nextX, y }, map)) x = nextX;
        const nextY = Math.max(HERO_RADIUS, Math.min(map.height - HERO_RADIUS, y + stepY));
        if (canStandAt({ x, y: nextY }, map)) y = nextY;
    }
    return { x, y };
}
