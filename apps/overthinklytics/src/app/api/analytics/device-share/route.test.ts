// <llm-snippet-file>apps/overthinklytics/src/app/api/analytics/device-share/route.test.ts</llm-snippet-file>
import {afterEach, describe, expect, it, vi} from 'vitest';
import {GET} from './route';
import {prisma} from '@/server/db';

vi.mock('@/server/db', () => ({
    prisma: {
        deviceShare: {
            findFirst: vi.fn(),
            findMany: vi.fn(),
        },
    },
}));

describe('GET /analytics/device-share', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('should return device share data with a snapshot date', async () => {
        const mockSnapshotDate = new Date('2023-10-01T00:00:00Z');
        const mockData = [
            {device: 'iPhone', sharePct: 50},
            {device: 'Android', sharePct: 30},
        ];

        prisma.deviceShare.findFirst.mockResolvedValue({
            snapshotDate: mockSnapshotDate,
        });

        prisma.deviceShare.findMany.mockResolvedValue(mockData);

        const req = new Request('http://localhost/api/analytics/device-share');
        const res = await GET(req);

        expect(res.status).toBe(200);
        const json = await res.json();
        expect(json).toEqual({
            data: [
                {name: 'iPhone', value: 50},
                {name: 'Android', value: 30},
            ],
            snapshotDate: mockSnapshotDate.toISOString(),
        });
    });

    it('should return device share data without a snapshot date in query', async () => {
        prisma.deviceShare.findFirst.mockResolvedValue(null);
        prisma.deviceShare.findMany.mockResolvedValue([]);

        const req = new Request('http://localhost/api/analytics/device-share');
        const res = await GET(req);

        expect(res.status).toBe(200);
        const json = await res.json();
        expect(json).toEqual({
            data: [],
            snapshotDate: undefined,
        });
    });

    it('should return 500 if an error occurs', async () => {
        prisma.deviceShare.findFirst.mockRejectedValue(new Error('Database error'));

        const req = new Request('http://localhost/api/analytics/device-share');
        const res = await GET(req);

        expect(res.status).toBe(500);
        const json = await res.json();
        expect(json).toEqual({
            error: 'Database error',
        });
    });

    it('should handle invalid date in query parameter', async () => {
        prisma.deviceShare.findFirst.mockResolvedValue(null);
        prisma.deviceShare.findMany.mockResolvedValue([]);

        const req = new Request('http://localhost/api/analytics/device-share?date=invalid-date');
        const res = await GET(req);

        expect(res.status).toBe(200);
        const json = await res.json();
        expect(json).toEqual({
            data: [],
            snapshotDate: undefined,
        });
    });
});
