import { LoginService } from './login.service';
import { environment } from '../../environments/environment';

describe('LoginService.encryptEmail', () => {
  const noop: any = () => {};

  // Minimal fake dependencies for constructing the service
  const fakeRouter: any = {};
  const fakeHttp: any = {};
  const fakeConstantService: any = { WEBSERVICE_URL: 'http://localhost:8080/api/' };
  const fakeTokenService: any = {};
  const fakeLogger: any = { log: noop };
  const fakeLocalStorage: any = { get: noop, set: noop };
  const fakeSharedHttpClientService: any = { handleError: noop };
  const fakeFirebaseAuthService: any = {};

  let service: LoginService;
  let cryptoSpy: any;

  beforeEach(() => {
    service = new LoginService(
      fakeRouter,
      fakeHttp,
      fakeConstantService,
      fakeTokenService,
      fakeLogger,
      fakeLocalStorage,
      fakeSharedHttpClientService,
      fakeFirebaseAuthService
    );
  });

  afterEach(() => {
    // Reset environment key
    (environment as any).loginValidateSharedKey = '';
    if (cryptoSpy) {
      try {
        cryptoSpy.and.callThrough();
      } catch (e) {
        /* ignore */
      }
      cryptoSpy = undefined;
    }
  });

  it('returns Base64(plaintext) when no shared key is configured', async () => {
    (environment as any).loginValidateSharedKey = '';
    const email = 'alice@example.com';
    const result = await (service as any).encryptEmail(email);
    expect(result).toBe(btoa(email));
  });

  it('returns Base64(IV||ciphertext) when shared key is configured (mocked Web Crypto)', async () => {
    // Prepare a 16-byte key (AES-128) and set in environment as Base64
    const keyBytes = new Uint8Array(16);
    for (let i = 0; i < 16; i++) keyBytes[i] = i + 1;
    const keyBin = String.fromCharCode(...Array.from(keyBytes));
    (environment as any).loginValidateSharedKey = btoa(keyBin);

    // Create a mock crypto object and replace the window.crypto getter
    const mockCrypto: any = {
      getRandomValues: (arr: Uint8Array) => {
        for (let i = 0; i < arr.length; i++) arr[i] = 1;
        return arr;
      },
      subtle: {
        importKey: async () => {
          return { _fakeKey: true } as any;
        },
        encrypt: async () => {
          const cipher = new Uint8Array([2, 3, 4]);
          return cipher.buffer;
        },
      },
    };

    cryptoSpy = spyOnProperty(window as any, 'crypto', 'get').and.returnValue(mockCrypto);

    const email = 'bob@example.com';
    const result = await (service as any).encryptEmail(email);

    // Expect Base64 of IV(12 bytes of 1) + [2,3,4]
    const iv = new Uint8Array(12).fill(1);
    const cipher = new Uint8Array([2, 3, 4]);
    const combined = new Uint8Array(iv.length + cipher.length);
    combined.set(iv, 0);
    combined.set(cipher, iv.length);
    const expected = btoa(String.fromCharCode(...Array.from(combined)));

    expect(result).toBe(expected);
  });
});
