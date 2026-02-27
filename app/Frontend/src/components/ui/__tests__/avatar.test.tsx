import { render, screen } from '@testing-library/react';
import { Avatar, AvatarFallback, AvatarImage } from '../avatar';

// Mock Radix Avatar Image since JSDOM doesn't load images
jest.mock('radix-ui', () => {
    const original = jest.requireActual('radix-ui');
    return {
        ...original,
        Avatar: {
            ...original.Avatar,
            Image: ({ alt, src }: any) => <img alt={alt} src={src} />,
            Fallback: ({ children }: any) => <div>{children}</div>,
            Root: ({ children }: any) => <span>{children}</span>,
        }
    };
});

describe('Avatar', () => {
  it('should render avatar correctly', () => {
    render(
      <Avatar>
        <AvatarImage src="https://example.com/photo.jpg" alt="Test User" />
        <AvatarFallback>TU</AvatarFallback>
      </Avatar>
    );
    
    expect(screen.getByAltText('Test User')).toBeDefined();
    expect(screen.getByText('TU')).toBeDefined();
  });
});
