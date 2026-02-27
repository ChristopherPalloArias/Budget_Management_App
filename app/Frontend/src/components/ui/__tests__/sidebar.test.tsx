import { render, screen, fireEvent } from '@testing-library/react';
import { SidebarProvider, Sidebar, SidebarTrigger, SidebarContent, SidebarGroup, SidebarMenu, SidebarMenuItem, SidebarMenuButton } from '../sidebar';

// Mock useIsMobile
jest.mock('@/hooks/use-mobile', () => ({
  useIsMobile: jest.fn().mockReturnValue(false)
}));

describe('Sidebar', () => {
  it('should render and toggle', () => {
    render(
      <SidebarProvider>
        <Sidebar>
          <SidebarContent>
            <SidebarGroup>
              <SidebarMenu>
                <SidebarMenuItem>
                  <SidebarMenuButton>Item 1</SidebarMenuButton>
                </SidebarMenuItem>
              </SidebarMenu>
            </SidebarGroup>
          </SidebarContent>
        </Sidebar>
        <SidebarTrigger />
      </SidebarProvider>
    );

    expect(screen.getByText('Item 1')).toBeDefined();
    
    const trigger = screen.getByRole('button', { name: /toggle sidebar/i });
    fireEvent.click(trigger);
    
    // Check if state changed (data-state on the wrapper or sidebar)
    // The SidebarProvider sets a cookie and changes internal state
    expect(document.cookie).toContain('sidebar_state=false');
  });

  it('should render mobile version when isMobile is true', () => {
    const { useIsMobile } = require('@/hooks/use-mobile');
    (useIsMobile as jest.Mock).mockReturnValue(true);

    render(
      <SidebarProvider>
        <Sidebar />
        <SidebarTrigger />
      </SidebarProvider>
    );

    // In mobile, it should render a Sheet (which we might need to mock or just check for its existence)
    // The Sheet title is "Sidebar" with sr-only
    expect(screen.getByText('Sidebar')).toBeDefined();
  });
});
