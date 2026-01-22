import { ClearinghousePage } from './app.po';

describe('clearinghouse App', () => {
  let page: ClearinghousePage;

  beforeEach(() => {
    page = new ClearinghousePage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
