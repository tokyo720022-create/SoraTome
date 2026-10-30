// MangaDex source plugin, for testing the SoraTome reader. Debug builds only.
// Uses MangaDex's public API directly. Only "safe" and "suggestive" ratings are requested.

const API = "https://api.mangadex.org";
const COVERS = "https://uploads.mangadex.org/covers/";
const REPORT_URL = "https://api.mangadex.network/report";
const RATINGS = "contentRating%5B%5D=safe&contentRating%5B%5D=suggestive";
const LANGUAGE = "en";
const SEARCH_PAGE_SIZE = 20;
const FEED_PAGE_SIZE = 100;
const MAX_FEED_REQUESTS = 30;

async function getJson(path) {
  return JSON.parse(await host.http.get(API + path));
}

function firstText(map) {
  if (!map) return null;
  if (map.en) return map.en;
  const values = Object.values(map);
  return values.length > 0 ? values[0] : null;
}

function coverUrl(manga) {
  const rels = manga.relationships || [];
  for (let i = 0; i < rels.length; i++) {
    const rel = rels[i];
    if (rel.type === "cover_art" && rel.attributes && rel.attributes.fileName) {
      return COVERS + manga.id + "/" + rel.attributes.fileName + ".512.jpg";
    }
  }
  return null;
}

function summary(manga) {
  return {
    id: manga.id,
    title: firstText(manga.attributes.title) || "Untitled",
    cover: coverUrl(manga),
  };
}

function chapterName(attrs) {
  const number = attrs.chapter;
  const title = attrs.title;
  if (number && title) return "Chapter " + number + " - " + title;
  if (number) return "Chapter " + number;
  return title || "Oneshot";
}

globalThis.plugin = {
  async search(query, page) {
    const offset = (page - 1) * SEARCH_PAGE_SIZE;
    let path = "/manga?limit=" + SEARCH_PAGE_SIZE + "&offset=" + offset +
      "&includes%5B%5D=cover_art&" + RATINGS;
    const text = String(query || "").trim();
    if (text) {
      path += "&title=" + encodeURIComponent(text) + "&order%5Brelevance%5D=desc";
    } else {
      path += "&order%5BfollowedCount%5D=desc";
    }
    const res = await getJson(path);
    return {
      items: res.data.map(summary),
      hasNext: offset + res.data.length < res.total,
    };
  },

  async getSeries(id) {
    const res = await getJson("/manga/" + encodeURIComponent(id) + "?includes%5B%5D=cover_art");
    const manga = res.data;
    const attrs = manga.attributes;
    const tags = (attrs.tags || [])
      .map(function (tag) { return firstText(tag.attributes.name); })
      .filter(function (name) { return !!name; })
      .slice(0, 12);
    return {
      id: manga.id,
      title: firstText(attrs.title) || "Untitled",
      description: firstText(attrs.description),
      cover: coverUrl(manga),
      tags: tags,
    };
  },

  async getChapters(seriesId) {
    const byNumber = {};
    const order = [];
    let offset = 0;

    for (let request = 0; request < MAX_FEED_REQUESTS; request++) {
      const path = "/manga/" + encodeURIComponent(seriesId) + "/feed?limit=" + FEED_PAGE_SIZE +
        "&offset=" + offset + "&translatedLanguage%5B%5D=" + LANGUAGE +
        "&order%5Bchapter%5D=asc&includeExternalUrl=0&includeEmptyPages=0&" + RATINGS;
      const res = await getJson(path);

      res.data.forEach(function (item) {
        const attrs = item.attributes;
        // Several groups can upload the same chapter. Keep the one with the most pages.
        const key = attrs.chapter ? "n" + attrs.chapter : "id" + item.id;
        const existing = byNumber[key];
        if (!existing) {
          byNumber[key] = item;
          order.push(key);
        } else if ((attrs.pages || 0) > (existing.attributes.pages || 0)) {
          byNumber[key] = item;
        }
      });

      offset += res.data.length;
      if (res.data.length === 0 || offset >= res.total) break;
    }

    return order.map(function (key) {
      const item = byNumber[key];
      const attrs = item.attributes;
      const number = attrs.chapter ? parseFloat(attrs.chapter) : NaN;
      const published = attrs.publishAt ? Date.parse(attrs.publishAt) : NaN;
      return {
        id: item.id,
        name: chapterName(attrs),
        number: isNaN(number) ? null : number,
        date: isNaN(published) ? null : published,
        url: "https://mangadex.org/chapter/" + item.id,
      };
    });
  },

  async getPages(chapterId) {
    const res = await getJson("/at-home/server/" + encodeURIComponent(chapterId));
    const files = res.chapter.data || [];
    if (files.length === 0) {
      throw new Error("This chapter has no readable pages here. It may be hosted on another site.");
    }
    const saver = res.chapter.dataSaver || [];
    const hash = res.chapter.hash;
    const base = res.baseUrl;
    // Only the MangaDex@Home network asks for load reports.
    const report = base.indexOf("mangadex.network") !== -1 ? REPORT_URL : null;

    return files.map(function (file, i) {
      return {
        index: i,
        url: base + "/data/" + hash + "/" + file,
        lowQualityUrl: saver.length === files.length ? base + "/data-saver/" + hash + "/" + saver[i] : null,
        reportUrl: report,
      };
    });
  },
};
