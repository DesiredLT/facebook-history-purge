from pathlib import Path

p=Path('app/src/main/java/lt/vaeloria/ooc/VaeloriaDb.java')
s=p.read_text(encoding='utf-8')

if 'private void migrateV4toV5(SQLiteDatabase db)' not in s:
    marker='    private static int parseInt(String s, int fallback)'
    methods='''    private void migrateV4toV5(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS mastery (name TEXT PRIMARY KEY, level INTEGER NOT NULL, xp INTEGER NOT NULL, next_xp INTEGER NOT NULL)");
        seedMastery(db);
    }

    private int masteryNext(int level){ return 300 + Math.max(0,Math.min(99,level))*10; }

    private void seedMastery(SQLiteDatabase db) {
        for (BaseStatCatalog.Group group : BaseStatCatalog.GROUPS) for (String stat : group.stats) {
            ContentValues v=new ContentValues();
            v.put("name",stat);v.put("level",70);v.put("xp",0);v.put("next_xp",masteryNext(70));
            db.insertWithOnConflict("mastery",null,v,SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

'''
    if marker not in s:
        raise SystemExit('VaeloriaDb parseInt insertion marker not found')
    s=s.replace(marker,methods+marker,1)

required=[
    'private void migrateV4toV5(SQLiteDatabase db)',
    'private int masteryNext(int level)',
    'private void seedMastery(SQLiteDatabase db)'
]
missing=[x for x in required if x not in s]
if missing:
    raise SystemExit('Missing mastery DB methods: '+', '.join(missing))

p.write_text(s,encoding='utf-8')
