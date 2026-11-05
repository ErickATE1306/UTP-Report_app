import { TestBed } from '@angular/core/testing';
import { LoadingService } from './loading.service';
import { ScrollLockService } from './scroll-lock.service';
import { NotificationService } from './notification.service';

describe('LoadingService', () => {
  let service: LoadingService;
  let actual: boolean;

  beforeEach(() => {
    service = TestBed.inject(LoadingService);
    service.isLoading$.subscribe(v => (actual = v));
  });

  afterEach(() => {
    service.reset();
    document.body.classList.remove('is-loading');
  });

  it('inicia sin carga activa', () => {
    expect(actual).toBeFalse();
  });

  it('show activa la carga y agrega la clase al body', () => {
    service.show();
    expect(actual).toBeTrue();
    expect(document.body.classList.contains('is-loading')).toBeTrue();
  });

  it('sigue cargando hasta que termina la última petición', () => {
    service.show();
    service.show();

    service.hide();
    expect(actual).toBeTrue();

    service.hide();
    expect(actual).toBeFalse();
    expect(document.body.classList.contains('is-loading')).toBeFalse();
  });

  it('hide de más no deja el contador en negativo', () => {
    service.hide();
    service.show();
    expect(actual).toBeTrue();
    service.hide();
    expect(actual).toBeFalse();
  });

  it('reset termina la carga aunque haya peticiones activas', () => {
    service.show();
    service.show();
    service.reset();
    expect(actual).toBeFalse();
  });
});

describe('ScrollLockService', () => {
  let service: ScrollLockService;

  beforeEach(() => {
    service = TestBed.inject(ScrollLockService);
    service.reset();
  });

  afterEach(() => service.reset());

  it('lock bloquea el scroll del body', () => {
    service.lock();
    expect(document.body.style.overflow).toBe('hidden');
    expect(service.isLocked()).toBeTrue();
  });

  it('mantiene el bloqueo hasta liberar todos los lock', () => {
    service.lock();
    service.lock();

    service.unlock();
    expect(document.body.style.overflow).toBe('hidden');

    service.unlock();
    expect(document.body.style.overflow).toBe('');
    expect(service.isLocked()).toBeFalse();
  });

  it('unlock de más no deja el contador en negativo', () => {
    service.unlock();
    service.lock();
    service.unlock();
    expect(service.isLocked()).toBeFalse();
  });

  it('reset libera todos los bloqueos', () => {
    service.lock();
    service.lock();
    service.reset();
    expect(service.isLocked()).toBeFalse();
  });
});

describe('NotificationService', () => {
  let audio: jasmine.SpyObj<HTMLAudioElement>;

  beforeEach(() => {
    audio = jasmine.createSpyObj<HTMLAudioElement>('Audio', ['load', 'play']);
    audio.play.and.resolveTo();
    spyOn(window, 'Audio').and.returnValue(audio);
    spyOn(console, 'log');
    spyOn(console, 'warn');
  });

  it('carga el sonido de notificación al crearse', () => {
    TestBed.inject(NotificationService);

    expect(window.Audio).toHaveBeenCalledOnceWith('/audio_notificacion/iphone-notificacion-.mp3');
    expect(audio.load).toHaveBeenCalled();
  });

  it('reinicia y reproduce el sonido', () => {
    const service = TestBed.inject(NotificationService);
    audio.currentTime = 12;

    service.playNotificationSound();

    expect(audio.currentTime).toBe(0);
    expect(audio.play).toHaveBeenCalled();
  });

  it('no lanza un error si el navegador bloquea la reproducción', async () => {
    audio.play.and.rejectWith(new Error('NotAllowedError'));
    const service = TestBed.inject(NotificationService);

    expect(() => service.playNotificationSound()).not.toThrow();
    await Promise.resolve();
    await Promise.resolve();
    expect(console.warn).toHaveBeenCalled();
  });
});
