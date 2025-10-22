import pino from 'pino'

const level = process.env.LOG_LEVEL ?? 'info'
const format = process.env.LOG_FORMAT ?? 'json' // json | pretty

export const logger = pino({
  name: 'overthinklytics-web',
  level,
  transport: format === 'pretty' ? {
    target: 'pino-pretty',
    options: { colorize: true, translateTime: true }
  } : undefined,
})

export function child(bindings: Record<string, unknown>) {
  return logger.child(bindings)
}
