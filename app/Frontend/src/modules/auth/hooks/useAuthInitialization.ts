import { useEffect, useRef, useCallback } from 'react';
import { useUserStore } from '../store/useUserStore';

export const useAuthInitialization = (): void => {
  const initAuthListener = useUserStore((state) => state.initAuthListener);
  const isInitialized = useRef(false);

  const handleInit = useCallback(() => {
    if (isInitialized.current) {
      return;
    }
    const unsubscribe = initAuthListener();
    isInitialized.current = true;
    return unsubscribe;
  }, [initAuthListener]);

  useEffect(() => {
    const unsubscribe = handleInit();
    return () => {
      if (unsubscribe) {
        unsubscribe();
        isInitialized.current = false;
      }
    };
  }, [handleInit]);
};
