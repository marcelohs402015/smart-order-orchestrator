interface LogContext {
  [key: string]: unknown;
}

const isDevelopment = import.meta.env.DEV;

export const logger = {
  log: (message: string, context?: LogContext) => {
    if (isDevelopment) {
      console.log(`[LOG] ${message}`, context || '');
    }
  },

  info: (message: string, context?: LogContext) => {
    if (isDevelopment) {
      console.info(`[INFO] ${message}`, context || '');
    }
  },

  warn: (message: string, context?: LogContext) => {
    if (isDevelopment) {
      console.warn(`[WARN] ${message}`, context || '');
    }
  },

  error: (message: string, error?: unknown, context?: LogContext) => {
    if (isDevelopment) {
      console.error(`[ERROR] ${message}`, error, context || '');
    }
  },
};

