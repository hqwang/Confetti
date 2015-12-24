package tyrantgit.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import tyrantgit.explosionfield.ExplosionField;


public class MainActivity extends Activity {

    private ExplosionField mExplosionField;
    private int mode = ExplosionField.MODE_CONFETTI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mExplosionField = ExplosionField.attach2Window(this);
        mExplosionField.addListener(findViewById(R.id.root));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean consumed = false;
        int flag = 0;

        switch (item.getItemId()) {
            case R.id.action_reset:
                consumed = true;
                break;
            case R.id.action_mode_explosion:
                consumed = true;
                mode = ExplosionField.MODE_EXPLOSION;
                break;
            case R.id.action_mode_confetti:
                consumed = true;
                mode = ExplosionField.MODE_CONFETTI;
                break;
            case R.id.action_mode_confetti_gravity:
                consumed = true;
                mode = ExplosionField.MODE_CONFETTI;
                flag = ExplosionField.FLAG_SUPPORT_GRAVITY;
                break;
        }

        if (consumed) {
            View root = findViewById(R.id.root);
            mExplosionField.reset(root);
            mExplosionField.addListener(root);
            mExplosionField.setMode(mode);
            if (flag > 0) {
                mExplosionField.setFlag(flag);
            }
            mExplosionField.clear();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
