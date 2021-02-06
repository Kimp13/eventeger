export function parseDate(dateString) {
  if (dateString && dateString.length >= 24) {
    const date = new Date(
      parseInt(dateString.substring(0, 4), 10),
      parseInt(dateString.substring(5, 7), 10),
      parseInt(dateString.substring(8, 10), 10),
      parseInt(dateString.substring(11, 13), 10),
      parseInt(dateString.substring(14, 16), 10),
      parseInt(dateString.substring(17, 19), 10),
      parseInt(dateString.substring(20, 23), 10)
    );

    if (!isNaN(date.getTime())) {
      return date;
    }
  }

  return false;
}

export function dateLess(date1, date2, strict = true) {
  if (strict) {
    return date1.getTime() < date2.getTime();
  }

  return date1.getTime() <= date2.getTime();
}

export function UTC() {
  const date = new Date();

  date.setTime(date.getTime() + date.getTimezoneOffset() * 6e4);

  return date;
}