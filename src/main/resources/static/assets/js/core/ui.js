(function () {
  window.EASUI = {
    toast(message) {
      // Keep behavior simple: use alert for now so old pages remain compatible.
      alert(message);
    },
    formatCurrency(value) {
      if (value === null || value === undefined || value === "") return "-";
      return Number(value).toLocaleString("vi-VN") + "đ";
    },
  };
})();
