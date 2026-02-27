import { render, screen, fireEvent } from '@testing-library/react';
import { SidebarProvider, Sidebar, SidebarTrigger, SidebarContent, SidebarGroup, SidebarMenu, SidebarMenuItem, SidebarMenuButton } from '../sidebar';

// Mock useIsMobile
jest.mock('@/hooks/use-mobile', () => ({
  useIsMobile: jest.fn().mockReturnValue(false)
}));

// Mock Sheet to always render content in tests
jest.mock('../sheet', () => ({
  Sheet: ({ children, open }: any) => <div data-testid="sheet" data-open={open}>{children}</div>,
  SheetContent: ({ children }: any) => <div>{children}</div>,
  SheetHeader: ({ children }: any) => <div>{children}</div>,
  SheetTitle: ({ children }: any) => <div>{children}</div>,
  SheetDescription: ({ children }: any) => <div>{children}</div>,
}));

describe('Sidebar', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    document.cookie = 'sidebar_state=; Max-Age=0';
  });

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

    // The Sheet title is "Sidebar" with sr-only
    // Check for "Sidebar" text in a way that handles the mock correctly
    expect(screen.queryByText('Sidebar')).toBeTruthy();
  });
});
