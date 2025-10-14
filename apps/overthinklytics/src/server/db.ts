import { PrismaClient } from '@prisma/client';

// Prisma client is kept in case future single-tenant features need DB access.
const globalForPrisma = global as unknown as { prisma?: PrismaClient };
export const prisma = globalForPrisma.prisma ?? new PrismaClient();
if (process.env.NODE_ENV !== 'production') globalForPrisma.prisma = prisma;

// Multitenancy removed: no schema bootstrap required.
export function ensureDbReady(): Promise<void> {
  return Promise.resolve();
}
