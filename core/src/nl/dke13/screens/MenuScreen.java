package nl.dke13.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class MenuScreen implements Screen{
	
	Skin skin;
	Stage stage;
	SpriteBatch batch;
	
	Game g;
	public MenuScreen(Game g){
		create();
		this.g = g;
	}
	
	public MenuScreen(){
		create();
	}
	private void create() {
		
		batch = new SpriteBatch();
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);
		
		Skin skin = new Skin();
		
		Pixmap pixmap = new Pixmap(100,100,Format.RGBA8888);
		pixmap.setColor(0,1,0,0.75f);
		Texture pixmaptex = new Texture( pixmap );
		pixmap.fill();
		
		skin.add("white", new Texture(pixmap));
		
		BitmapFont bfont = new BitmapFont();
		skin.add("default", bfont);
		
		TextButtonStyle textButtonStyle = new TextButtonStyle();
		//textButtonStyle.up = skin.newDrawable("white", "core/assets/tiger_woods.png");
		//textButtonStyle.down = skin.newDrawable("white", Color.DARK_GRAY);
		//textButtonStyle.checked = skin.newDrawable("white", Color.BLUE);
		//textButtonStyle.over = skin.newDrawable("white", Color.LIGHT_GRAY);
		
		textButtonStyle.font = skin.getFont("default");
		
		skin.add("default",  textButtonStyle);
		
		final TextButton textButton = new TextButton("PLAY", textButtonStyle);
		textButton.setPosition(200, 200);
		stage.addActor(textButton);
		stage.addActor(textButton);
		stage.addActor(textButton);
		
		textButton.addListener(new ChangeListener(){

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				System.out.println("hey :))))");
				textButton.setText("Starting new game");
				g.setScreen(new CrazyGolf());
			}
		});
			
		}
		
	public void render(float delta){
		
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1/30f));
		stage.draw();
		//Table.drawDebug(stage);
	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		
		stage.dispose();
		skin.dispose();
		
	}


}