export interface RestReport {
    message: string;
    status: "SUCCESS" | "AMBUSH";
    monsterId?: number;
}
