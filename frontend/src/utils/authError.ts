import axios from "axios";

interface ApiErrorPayload {
    message?: string;
    fieldErrors?: Record<string, string> | string[];
}

export function getAuthError(error: unknown, fallback: string): string {
    if (!axios.isAxiosError<ApiErrorPayload | string>(error)) return fallback;
    const responseData = error.response?.data;
    if (typeof responseData === "string") {
        const message = responseData.trim();
        if (message && !message.startsWith("<!DOCTYPE") && !message.startsWith("<html")) {
            return message;
        }
    }
    const data = typeof responseData === "object" ? responseData : undefined;
    if (Array.isArray(data?.fieldErrors) && data.fieldErrors.length > 0) return data.fieldErrors[0];
    if (data?.fieldErrors && !Array.isArray(data.fieldErrors)) {
        const firstFieldError = Object.values(data.fieldErrors)[0];
        if (firstFieldError) return firstFieldError;
    }
    if (data?.message) return data.message;
    if (!error.response) return "Сервер недоступен. Проверьте соединение и попробуйте снова.";
    return fallback;
}
