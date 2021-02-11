import bs from 'binary-search';

export const sortedHas = (array, element) => {
  return bs(
    array,
    element,
    function comparator(element, needle) {
      return element - needle;
    }
  ) >= 0;
};

export const arrayOf = (start, end) => {
  const result = [];

  while (start < end) {
    result.push(start++);
  }

  return result;
};

export const shiftForward = (array, beginning) => {
  for (let i = array.length - 1; i > 0; i -= 1) {
    array[i] = array[i - 1];
  }

  array[0] = beginning;
}