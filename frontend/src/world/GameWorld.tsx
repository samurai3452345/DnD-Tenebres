import { useEffect, useEffectEvent, useLayoutEffect, useMemo, useRef, useState, type PointerEvent } from "react";
import { preload } from "react-dom";
import { HERO_RADIUS, moveHero, type MapCollision, type Point } from "./collision";
import { getWorldMap } from "./maps";
import "./game-world.css";

const directions: Record<string, Point> = {
    ArrowUp: { x: 0, y: -1 }, KeyW: { x: 0, y: -1 },
    ArrowDown: { x: 0, y: 1 }, KeyS: { x: 0, y: 1 },
    ArrowLeft: { x: -1, y: 0 }, KeyA: { x: -1, y: 0 },
    ArrowRight: { x: 1, y: 0 }, KeyD: { x: 1, y: 0 },
};
const walkSpeed = 430;
const pickupReach = 56;
export const pickupDurationMs = 2000;

type Facing = "down" | "up" | "right" | "left";

interface HeroState extends Point {
    facing: Facing;
    moving: boolean;
}

interface WalkTarget extends Point {
    pickupKey?: string;
}

function canReachPickup(position: Point, target: Point, map: MapCollision) {
    const offset = { x: target.x - position.x, y: target.y - position.y };
    if (Math.hypot(offset.x, offset.y) > pickupReach) return false;
    const endpoint = moveHero(position, offset, map);
    return Math.hypot(endpoint.x - target.x, endpoint.y - target.y) < 0.5;
}

function getFacing(offsetX: number, offsetY: number, previous: Facing): Facing {
    const horizontal = Math.abs(offsetX);
    const vertical = Math.abs(offsetY);
    if (horizontal > vertical || (horizontal === vertical && (previous === "left" || previous === "right"))) {
        return offsetX < 0 ? "left" : "right";
    }
    return offsetY < 0 ? "up" : "down";
}

interface GameWorldProps {
    locationId: string;
    background: string;
    paused: boolean;
    pickingUp: boolean;
    showCollisions: boolean;
    collectedPickups: ReadonlySet<string>;
    onPickup?: (key: string) => void;
}

export default function GameWorld({
    locationId, background, paused, pickingUp, showCollisions, collectedPickups, onPickup,
}: GameWorldProps) {
    const map = useMemo(() => getWorldMap(locationId, background), [locationId, background]);
    const viewport = useRef<HTMLDivElement>(null);
    const destination = useRef<WalkTarget | null>(null);
    const [hero, setHero] = useState<HeroState>(() => ({ ...map.spawn, facing: "down", moving: false }));
    const heroState = useRef(hero);
    const [size, setSize] = useState({ width: 0, height: 0 });
    const pickupReached = useEffectEvent((key: string) => onPickup?.(key));

    useEffect(() => {
        preload("/assets/game/knight-walk-sprites.png", { as: "image" });
        preload("/assets/game/knight-pickup-sprites.png", { as: "image" });
    }, []);

    useLayoutEffect(() => {
        const element = viewport.current;
        if (!element) return;
        const measure = () => setSize({ width: element.clientWidth, height: element.clientHeight });
        measure();
        const observer = new ResizeObserver(measure);
        observer.observe(element);
        return () => observer.disconnect();
    }, []);

    useEffect(() => {
        if (paused) return;

        const keys = new Set<string>();
        let frame: number;
        let lastTime: number | undefined;

        const onKeyDown = (event: KeyboardEvent) => {
            if (!directions[event.code] || event.ctrlKey || event.altKey || event.metaKey) return;
            if (event.target instanceof HTMLElement &&
                event.target.closest("input, textarea, select, [contenteditable=true]")) return;
            event.preventDefault();
            keys.add(event.code);
            destination.current = null;
        };
        const onKeyUp = (event: KeyboardEvent) => keys.delete(event.code);
        const resetInput = () => {
            keys.clear();
            destination.current = null;
        };

        function tick(time: number) {
            const stride = walkSpeed * Math.min(0.05, (time - (lastTime ?? time)) / 1000);
            lastTime = time;
            const current = heroState.current;
            let offsetX = 0;
            let offsetY = 0;
            for (const key of keys) {
                offsetX += directions[key].x;
                offsetY += directions[key].y;
            }
            const target = keys.size === 0 ? destination.current : null;
            if (target) {
                offsetX = target.x - current.x;
                offsetY = target.y - current.y;
            }

            const distance = Math.hypot(offsetX, offsetY);
            const facing = distance > 0 ? getFacing(offsetX, offsetY, current.facing) : current.facing;
            const alreadyNearPickup = target?.pickupKey !== undefined && canReachPickup(current, target, map);
            let next: Point = current;
            if (!alreadyNearPickup && distance > 0 && stride > 0) {
                const factor = keys.size > 0 ? stride / distance : Math.min(1, stride / distance);
                next = moveHero(current, { x: offsetX * factor, y: offsetY * factor }, map);
            }
            const moved = next.x !== current.x || next.y !== current.y;
            const pickupKey = target?.pickupKey !== undefined &&
                (alreadyNearPickup || (moved && canReachPickup(next, target, map))) ? target.pickupKey : undefined;
            const moving = moved && pickupKey === undefined;
            if (pickupKey !== undefined || (!moved && stride > 0)) destination.current = null;
            if (moved || current.moving !== moving || current.facing !== facing) {
                const updated = { ...next, facing, moving };
                heroState.current = updated;
                setHero(updated);
            }
            if (pickupKey !== undefined) pickupReached(pickupKey);
            frame = requestAnimationFrame(tick);
        }

        window.addEventListener("keydown", onKeyDown);
        window.addEventListener("keyup", onKeyUp);
        window.addEventListener("blur", resetInput);
        frame = requestAnimationFrame(tick);
        return () => {
            cancelAnimationFrame(frame);
            window.removeEventListener("keydown", onKeyDown);
            window.removeEventListener("keyup", onKeyUp);
            window.removeEventListener("blur", resetInput);
            resetInput();
        };
    }, [map, paused]);

    const scale = Math.max(size.width / map.width, size.height / map.height) || 1;
    const left = Math.min(0, Math.max(size.width - map.width * scale, size.width / 2 - hero.x * scale));
    const top = Math.min(0, Math.max(size.height - map.height * scale, size.height / 2 - hero.y * scale));

    function walkTo(event: PointerEvent<HTMLDivElement>) {
        if (paused || !event.isPrimary || event.button !== 0) return;
        const box = event.currentTarget.getBoundingClientRect();
        destination.current = {
            x: (event.clientX - box.left - left) / scale,
            y: (event.clientY - box.top - top) / scale,
        };
    }

    return (
        <div
            className="game-world"
            ref={viewport}
            role="region"
            aria-label="Игровая карта. Движение стрелками, WASD или нажатием на карту."
            onPointerDown={walkTo}
        >
            <div
                className="game-world__map"
                style={{
                    width: map.width,
                    height: map.height,
                    transform: `translate(${left}px, ${top}px) scale(${scale})`,
                }}
            >
                <img className="game-world__art" src={map.ground} alt="" draggable={false} />
                {onPickup && map.pickups.map(({ id, x, y }) => {
                    const pickupKey = `${locationId}:${id}`;
                    if (collectedPickups.has(pickupKey)) return null;
                    return (
                        <button
                            key={pickupKey}
                            type="button"
                            className="game-world__pickup"
                            style={{ left: x, top: y }}
                            aria-label="Обыскать место"
                            disabled={paused}
                            onPointerDown={(event) => event.stopPropagation()}
                            onClick={() => { destination.current = { x, y, pickupKey }; }}
                        />
                    );
                })}
                <div
                    className="game-world__hero"
                    style={{
                        left: hero.x,
                        top: hero.y,
                        animationDuration: pickingUp ? `${pickupDurationMs}ms` : undefined,
                    }}
                    data-facing={hero.facing}
                    data-moving={hero.moving && !paused}
                    data-paused={paused}
                    data-picking-up={pickingUp}
                    aria-hidden="true"
                />
                {map.foreground && <img className="game-world__art" src={map.foreground} alt="" draggable={false} />}
                {showCollisions && (
                    <div
                        className="game-world__collision-overlay"
                        style={{ boxShadow: `inset 0 0 0 ${HERO_RADIUS}px var(--world-collision-edge)` }}
                        aria-hidden="true"
                    >
                        {map.obstacles.map(({ points }, index) => (
                            <div
                                key={index}
                                style={{ clipPath: `polygon(${points.map(({ x, y }) => `${x}px ${y}px`).join(", ")})` }}
                            />
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}
