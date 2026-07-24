# Design QA

- Source visual truth: `design-reference/selected-home-design.png`
- Desktop implementation: `design-qa-assets/implementation-desktop.png`
- Mobile implementation: `design-qa-assets/implementation-mobile.png`
- Full-view comparison: `design-qa-assets/comparison-full.png`
- Focused comparison: `design-qa-assets/comparison-focus.png`
- Desktop viewport: 1440 × 1024
- Mobile viewport: 390 × 844
- State: home route, logged out, empty prompt, smart mode, ready status

## Full-view comparison evidence

The normalized side-by-side comparison shows the same information hierarchy and major geometry as the selected design: 68px navigation, centered two-part headline, supporting copy, a single dominant prompt composer, three inspiration actions, and a bottom copyright area. The generated grid-and-atmosphere asset preserves the selected design's cyan and lavender background balance without embedding editable interface text.

## Focused comparison evidence

The focused hero/composer comparison was required because typography, prompt proportions, control alignment, radii, and icons were too small to judge reliably in the full view. The final implementation matches the source's wide headline, 24px composer radius, large empty writing surface, separated bottom toolbar, right-aligned settings/generate actions, and restrained elevation. Ant Design icons replace all visible UI iconography; no placeholder or handcrafted SVG icon is used.

## Required fidelity surfaces

- Fonts and typography: Chinese system sans-serif stack with PingFang SC and Microsoft YaHei fallbacks; display weight, line height, letter spacing, responsive wrapping, and body scale visually match the reference. Mobile heading intentionally becomes two lines.
- Spacing and layout rhythm: desktop header, hero, composer, inspiration row, and 96px footer align with the normalized reference. Desktop `scrollHeight` and `clientHeight` are both 1024, so there is no unintended first-screen overflow.
- Colors and visual tokens: deep navy text, Ant primary blue, teal accent, cool-gray supporting text, and low-contrast borders map to the reference palette. The title uses discrete blue/teal spans rather than an inaccessible decorative text image.
- Image quality and asset fidelity: existing brand PNG is preserved; the generated 1536 × 1024 hero background was inspected and compressed to an 11KB WebP without visible artifacts. No target image asset is represented by CSS art or a placeholder.
- Copy and content: headline, prompt placeholder, menu labels, inspiration examples, login action, and exact footer wording match the selected direction and product context.
- Icons: consistent `@ant-design/icons-vue` icons are used throughout. The small star icon is the closest library equivalent to the mock's four-point sparkle and is an acceptable P3 variation.
- Responsiveness and accessibility: mobile width has no horizontal overflow (`scrollWidth` equals `clientWidth`); controls stack without clipping; semantic headings, labels, alt text, keyboard focus, Ctrl/Cmd+Enter generation, and practical touch targets are present.

## Interaction and browser checks

- Navigation to `/about`: passed.
- Inspiration example populates the prompt: passed.
- Generation settings popover opens and both switches work: passed.
- Generation flow reaches the visible `任务已创建` success state: passed.
- Empty prompt warning: implemented.
- Browser console errors: none.
- Vue DevTools overlay: removed from the development build because it obscured mobile content.

## Comparison history

### Iteration 1

- [P2] Desktop composition was wider than the selected mock and the document exceeded the viewport by 10px.
  - Fix: reduced the hero and inspiration maximum widths, adjusted the composer/section spacing, aligned header padding, and balanced the 96px footer against the hero height.
  - Post-fix evidence: `design-qa-assets/implementation-desktop.png`; final page metrics are 1440 × 1024 with no horizontal or vertical overflow.
- [P2] The default Vue DevTools floating control obscured the mobile inspiration list.
  - Fix: removed `vite-plugin-vue-devtools` from Vite configuration and project dependencies.
  - Post-fix evidence: `design-qa-assets/implementation-mobile.png`; no DevTools overlay is present and mobile horizontal overflow is zero.

## Findings

No actionable P0, P1, or P2 differences remain.

## Follow-up polish

- [P3] The Ant Design five-point star is slightly different from the mock's four-point sparkle, but it preserves the intended emphasis and icon consistency.
- [P3] The generated mock uses a continuous color transition in a few accents; the implementation uses native solid brand colors for sharper rendering and maintainability.

## Implementation checklist

- [x] Faithful desktop composition
- [x] Responsive mobile layout
- [x] Real generated background asset
- [x] Consistent icon library
- [x] Working primary interactions
- [x] Navigation verified
- [x] Console checked
- [x] Type-check, ESLint, and production build passed

final result: passed
