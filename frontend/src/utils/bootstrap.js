// Bootstrap utility functions for manual component initialization
// Only needed for components that require manual initialization

// Initialize all tooltips on page
export const initTooltips = () => {
  const tooltipTriggerList = document.querySelectorAll(
    '[data-bs-toggle="tooltip"]'
  );
  const tooltipList = [...tooltipTriggerList].map(
    (tooltipTriggerEl) => new Tooltip(tooltipTriggerEl)
  );
  return tooltipList;
};

// Initialize all popovers on page
export const initPopovers = () => {
  const popoverTriggerList = document.querySelectorAll(
    '[data-bs-toggle="popover"]'
  );
  const popoverList = [...popoverTriggerList].map(
    (popoverTriggerEl) => new Popover(popoverTriggerEl)
  );
  return popoverList;
};

// Create a modal instance
export const createModal = (element) => {
  return new Modal(element);
};

// Create a toast instance
export const createToast = (element) => {
  return new Toast(element);
};
