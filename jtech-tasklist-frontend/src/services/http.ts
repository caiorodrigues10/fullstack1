export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE';

export interface ApiError {
	message: string;
	status: number;
	details?: unknown;
}

const DEFAULT_BASE_URL = 'http://localhost:8080';

function getBaseUrl(): string {
	const fromEnv = import.meta.env.VITE_API_BASE_URL as string | undefined;
	return (fromEnv && fromEnv.trim().length > 0) ? fromEnv : DEFAULT_BASE_URL;
}

export async function apiFetch<TResponse>(
	path: string,
	options?: {
		method?: HttpMethod;
		body?: unknown;
		headers?: Record<string, string>;
		signal?: AbortSignal;
	}
): Promise<TResponse> {
	const url = new URL(path.startsWith('/') ? path : `/${path}`, getBaseUrl()).toString();
	const isJsonBody = options?.body !== undefined;

	const response = await fetch(url, {
		method: options?.method ?? 'GET',
		body: isJsonBody ? JSON.stringify(options?.body) : undefined,
		headers: {
			...(isJsonBody ? { 'Content-Type': 'application/json' } : {}),
			...(options?.headers ?? {})
		},
		signal: options?.signal
	});

	const contentType = response.headers.get('Content-Type') ?? '';
	const isJson = contentType.includes('application/json');

	if (!response.ok) {
		let errorPayload: unknown;
		try {
			errorPayload = isJson ? await response.json() : await response.text();
		} catch {
			errorPayload = undefined;
		}

		// Tenta extrair a mensagem do formato da API
		// Formato esperado: { status, timestamp, message, debugMessage }
		let errorMessage = 'Request failed';
		if (errorPayload && typeof errorPayload === 'object') {
			const payload = errorPayload as { message?: string; debugMessage?: string; status?: string };
			// Prioriza message, depois debugMessage
			if (payload.message && payload.message.trim()) {
				errorMessage = payload.message.trim();
			} else if (payload.debugMessage && payload.debugMessage.trim()) {
				errorMessage = payload.debugMessage.trim();
			}
		} else if (typeof errorPayload === 'string') {
			errorMessage = errorPayload;
		}

		const apiError: ApiError = {
			message: errorMessage,
			status: response.status,
			details: errorPayload
		};

		console.log('API Error:', apiError);
		throw apiError;
	}

	if (response.status === 204) {
		return undefined as unknown as TResponse;
	}

	if (isJson) {
		return (await response.json()) as TResponse;
	}

	// Fallback to text for non-JSON success responses
	return (await response.text()) as unknown as TResponse;
}


