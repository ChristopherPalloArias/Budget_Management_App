import { render, screen } from '@testing-library/react';
import { Avatar, AvatarFallback, AvatarImage } from '../avatar';

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
