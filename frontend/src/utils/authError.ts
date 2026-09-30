import axios from "axios";

interface ApiErrorPayload {
    message?: string;
    fieldErrors?: Record<string, string> | string[];
}

export function getAuthError(error: unknown, fallback: string): string {
    if (!axios.isAxiosError<ApiErrorPayload>(error)) return fallback;
    const data = error.response?.data;
    if (Array.isArray(data?.fieldErrors) && data.fieldErrors.length > 0) return data.fieldErrors[0];
    if (data?.fieldErrors && !Array.isArray(data.fieldErrors)) return Object.values(data.fieldErrors)[0] ?? fallback;
    if (data?.message) return data.message;
    if (!error.response) return "Сервер недоступен. Проверьте соединение и попробуйте снова.";
    return fallback;
}
