// auth.spec.ts
import {describe, expect, test, vi} from 'vitest';
import {getUserIdFromRequest} from '../server/auth';
import * as setup from '../../test/setup';

describe('getUserIdFromRequest', () => {
    test('returns user ID when X-Demo-User-Id header is present', async () => {
        vi.spyOn(setup, 'headers').mockResolvedValue(new Map([['x-demo-user-id', '12345']]));

        const userId = await getUserIdFromRequest();
        expect(userId).toBe('12345');
    });

    test('returns null when X-Demo-User-Id header is not present', async () => {
        vi.spyOn(setup, 'headers').mockResolvedValue(new Map());

        const userId = await getUserIdFromRequest();
        expect(userId).toBeNull();
    });

    test('returns null when X-Demo-User-Id header is empty', async () => {
        vi.spyOn(setup, 'headers').mockResolvedValue(new Map([['x-demo-user-id', '']]));

        const userId = await getUserIdFromRequest();
        expect(userId).toBeNull();
    });

    test('returns null when X-Demo-User-Id header contains only whitespace', async () => {
        vi.spyOn(setup, 'headers').mockResolvedValue(new Map([['x-demo-user-id', '   ']]));

        const userId = await getUserIdFromRequest();
        expect(userId).toBeNull();
    });
});
