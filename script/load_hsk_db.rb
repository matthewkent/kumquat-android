# encoding: utf-8

##
# based on CSV files from here: http://lingomi.com/blog/hsk-lists-2010/
#
require 'csv'
require 'sqlite3'

def insert_translation(db, order, level, word, pinyin, definition)
  db.execute("INSERT INTO translations VALUES(NULL, ?, NULL, ?, ?)", word, pinyin, definition)
  insert_hsk_entry(db, db.last_insert_row_id, order, level)
end

def insert_hsk_entry(db, id, order, level)
  db.execute("INSERT INTO hsk_lists VALUES(NULL, ?, ?, ?)", id, level, order)
end  

files = 1.upto(6).collect{|i| "hsk_files/HSK_Level_#{i}_(New_HSK).csv"}
cedict_file = "hsk_files/cedict_1_0_ts_utf8_mdbg.txt"

db = SQLite3::Database.new("tmp/hsk.db")
db.execute("PRAGMA synchronous=OFF")
db.execute("PRAGMA encoding=\"UTF-8\"")

create_tables = <<-STR
CREATE TABLE android_metadata (
  locale text
);
INSERT INTO android_metadata VALUES('en_US');
CREATE TABLE translations(
  _id integer primary key autoincrement,
  simplified text,
  traditional text,
  pinyin text,
  definition text
);
CREATE INDEX simplified ON translations(simplified);
CREATE TABLE hsk_lists (
  _id integer primary key autoincrement,
  translation_id integer,
  level_number integer,
  order_number integer
);
CREATE INDEX translation_id ON hsk_lists(translation_id);
CREATE INDEX level_order ON hsk_lists(level_number, order_number);
STR
db.execute_batch(create_tables)

db.execute("BEGIN")

hsk_words = []

files.each_with_index do |f, idx|
  level = idx + 1
  row_count = 0

  # files 1 through 5:
  # 0      1            2          3       4
  # order, level-order, simp word, pinyin, def
  #
  # file 6:
  # 0      1          2       3
  # level, simp word, pinyin, def
  #
  CSV.foreach(f, encoding: "UTF-8") do |row|
    # the files have a comment and a header so skip the first two lines
    row_count += 1
    next if row_count <= 2

    # some files inexplicably contain duplicate order numbers so we
    #  assign order based on sequence assuming the files are ordered
    order = row_count - 2

    # deal with inconsistent file format
    if level == 6
      word = row[1]
      pinyin = row[2]
      definition = row[3]
    else
      word = row[2]
      pinyin = row[3]
      definition = row[4]
    end

    next if word.nil? || word.empty?

    word.strip!
    pinyin.strip!
    definition.strip!

    # strip the ellipses from this one entry
    if word == "…分之…"
      word = word[1..2]
    end

    result = db.get_first_row("SELECT _id FROM translations WHERE simplified = ?", word)
    if result
      insert_hsk_entry(db, result.first, order, level)
    else
      insert_translation(db, order, level, word, pinyin, definition)
    end

    hsk_words << word
  end
end

cedict_data = {}
File.open(cedict_file).each do |line|
  if m = line.match(/^\s*(.+)\s+(.+)\s+\[(.+)\]\s+\/(.+)\/\s*$/)
    traditional = m[1]
    simplified = m[2]
    cedict_data[simplified.unpack("H*").first] = traditional
  end
end

hsk_words.each do |simplified|
  traditional = cedict_data[simplified.unpack("H*").first]
  if traditional.nil?
    traditional = ""
    simplified.chars do |c|
      traditional << cedict_data[c.unpack("H*").first]
    end
  end
  db.execute("UPDATE translations SET traditional = ? WHERE simplified = ?", traditional, simplified)
end

db.execute("COMMIT")
db.close
