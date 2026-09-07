package lt.vaeloria.ooc;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import java.util.Arrays;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class OpenAiFlowDeviceTest {
    private ActivityScenario<PolishedActivity> scenario;
    private Context context;

    @Before public void prepare(){
        context=ApplicationProvider.getApplicationContext();context.deleteDatabase("vaeloria.db");
        OpenAiSettings.select(context,OpenAiSettings.LOCAL);
        SecureKeyStore.clearSecret(context,OpenAiSettings.SECRET);
        context.getSharedPreferences("vaeloria_visual",0).edit().putBoolean("animations",false).commit();
        scenario=ActivityScenario.launch(PolishedActivity.class);
        scenario.onActivity(a->{assertTrue(a.applyCharacterProfile("Austėja",27,false,"moteris","Sidabrinis apsiaustas",
                "akademija","arkanistas",Arrays.asList("smalsumas","drausme","atjauta")));a.show("game");});
    }
    @After public void cleanup(){
        if(scenario!=null)scenario.close();SecureKeyStore.clearSecret(context,OpenAiSettings.SECRET);
        SecureKeyStore.clear(context);OpenAiSettings.select(context,OpenAiSettings.LOCAL);
    }

    @Test public void pendingNarrationLocksMutationAndLateAnswerAfterCancelIsIgnored(){
        scenario.onActivity(a->{try{
            String before=a.db.exportSave();long checkpoint=a.db.checkpoint("ankstesnis žingsnis",a.state);
            pending(a);JSONObject old=a.pendingResolvedTurn;
            a.show("settings");assertEquals("game",a.screen);
            View cancel=a.root.findViewWithTag("cancel_narration");assertNotNull(cancel);assertTrue(cancel.isEnabled());
            a.cancelPendingAction();assertFalse(a.busy);assertNull(a.pendingResolvedTurn);
            a.finish("Ištirti žymes",old,null);
            assertEquals(before,a.db.exportSave());
            try(android.database.Cursor cursor=a.db.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM checkpoints WHERE id=?",new String[]{String.valueOf(checkpoint)})){
                assertTrue(cursor.moveToFirst());assertEquals(1,cursor.getInt(0));
            }
            a.show("settings");assertTrue(hasText(a.root,"PRIJUNGTI OPENAI"));assertTrue(hasText(a.root,"ŽAISTI BE DI"));
        }catch(Exception error){throw new AssertionError(error);}});
    }

    @Test public void failedDatabaseCommitRollsBackExperienceMasteryWorldAndState(){
        scenario.onActivity(a->{try{
            String before=a.db.exportSave();pending(a);
            a.pendingCheck=new StatEngine.Check();a.pendingCheck.primary="Analitinis mąstymas";a.pendingCheck.secondary="Pastabumas";a.pendingCheck.outcome="sėkmė";
            a.pendingPrimaryXp=30;a.pendingSecondaryXp=15;
            a.db.getWritableDatabase().execSQL("CREATE TRIGGER fail_turn BEFORE UPDATE ON state BEGIN SELECT RAISE(ABORT,'device test'); END");
            a.finish("Ištirti manifestą",a.pendingResolvedTurn,null);
            a.db.getWritableDatabase().execSQL("DROP TRIGGER fail_turn");
            assertFalse(a.busy);assertEquals(before,a.db.exportSave());assertTrue(a.feedback.contains("išsaugoti nepavyko"));
        }catch(Exception error){throw new AssertionError(error);}});
    }

    @Test public void savedConnectionUsesKeystoreAndNeverEntersGameExports() {
        scenario.onActivity(a->{try{
            String code="test_device_connection_code_123456789012345";
            SecureKeyStore.save(a,"gsk_test_legacy_key_123456789012345");
            OpenAiSettings.save(a,"https://game.example",code);
            assertEquals(code,OpenAiSettings.load(a).token);assertEquals(OpenAiSettings.OPENAI,OpenAiSettings.provider(a));
            assertTrue(SecureKeyStore.load(a).startsWith("gsk_test_"));
            String stored=a.getSharedPreferences("vaeloria_secure",0).getString(OpenAiSettings.SECRET,"");
            assertFalse(stored.contains(code));assertFalse(stored.contains("game.example"));
            assertFalse(a.db.exportSave().contains(code));assertFalse(a.db.exportSave().contains("gsk_test_"));
            a.show("settings");assertTrue(hasText(a.root,"PATIKRINTI OPENAI RYŠĮ"));
            SecureKeyStore.clearSecret(a,OpenAiSettings.SECRET);assertEquals("",OpenAiSettings.load(a).token);
            assertTrue(SecureKeyStore.load(a).startsWith("gsk_test_"));
        }catch(Exception error){throw new AssertionError(error);}});
    }

    @Test public void remoteOrUnavailableNpcDoesNotReceiveConversationMemory(){
        scenario.onActivity(a->{
            GameState state=a.state;state.worldMinute=720;state.location="Veyrhold";
            assertEquals("",a.db.world().recordNpcInteraction("Pasikalbėti su Brynja","social",state));
            state.location="Luminara";
            assertEquals("",a.db.world().recordNpcInteraction("Ištirti Brynja įrangą","discovery",state));
            state.worldMinute=120;
            assertEquals("",a.db.world().recordNpcInteraction("Pasikalbėti su Brynja","social",state));
        });
    }

    private void pending(PolishedActivity a)throws Exception{
        a.pendingCheckpointId=a.db.checkpoint("prieš pasakojimą",a.state);
        a.pendingResolvedTurn=a.local("Ištirti manifestą",null);a.busy=true;a.show("game");
    }
    private boolean hasText(View view,String text){
        if(view instanceof TextView&&((TextView)view).getText().toString().contains(text))return true;
        if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++)if(hasText(group.getChildAt(i),text))return true;}return false;
    }
}
