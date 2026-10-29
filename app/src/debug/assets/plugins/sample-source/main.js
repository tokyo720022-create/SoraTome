// Sample source plugin for developing the SoraTome plugin runtime.
// It serves a few made-up series from memory and never touches the network.
// It is only bundled into debug builds.

const SERIES = [
  { id: "demo-1", title: "The Lighthouse Keeper (demo)", chapters: 3, tags: ["demo", "drama"],
    description: "Made-up demo data used to test the plugin runtime." },
  { id: "demo-2", title: "Paper Boats (demo)", chapters: 2, tags: ["demo", "slice of life"],
    description: "Another made-up series. Nothing here is real content." },
  { id: "demo-3", title: "Night Train to Nowhere (demo)", chapters: 4, tags: ["demo", "mystery"],
    description: "A third demo entry with a few more chapters." },
];

function findSeries(id) {
  const series = SERIES.find(function (s) { return s.id === id; });
  if (!series) throw new Error("Unknown series: " + id);
  return series;
}

globalThis.plugin = {
  async search(query, page) {
    host.log("search " + JSON.stringify(query) + " page " + page);
    const needle = String(query || "").toLowerCase();
    const items = SERIES
      .filter(function (s) { return s.title.toLowerCase().indexOf(needle) !== -1; })
      .map(function (s) { return { id: s.id, title: s.title, cover: null }; });
    return { items: items, hasNext: false };
  },

  async getSeries(id) {
    const s = findSeries(id);
    return { id: s.id, title: s.title, description: s.description, cover: null, tags: s.tags };
  },

  async getChapters(seriesId) {
    const s = findSeries(seriesId);
    const chapters = [];
    for (let i = 1; i <= s.chapters; i++) {
      chapters.push({ id: s.id + "/ch-" + i, name: "Chapter " + i, number: i, date: null });
    }
    return chapters;
  },

  async getPages(chapterId) {
    const pages = [];
    for (let i = 0; i < 5; i++) {
      pages.push({ index: i, url: "https://example.org/demo/" + chapterId + "/" + (i + 1) + ".png" });
    }
    return pages;
  },
};
