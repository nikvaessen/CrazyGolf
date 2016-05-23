package nl.dke12.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;

/**
 * Checks human input and feeds it to GameController
 */
public class HumanInputProcessor implements InputProcessor
{
    //GameController
    public boolean moveCamLeft()
    {
        return Gdx.input.isKeyPressed(Actions.MOVE_CAMERA_LEFT);
    }

    public boolean moveCamBack()
    {
        return Gdx.input.isKeyPressed(Actions.MOVE_CAMERA_BACK);
    }

    public boolean moveCamRight()
    {
        return Gdx.input.isKeyPressed(Actions.MOVE_CAMERA_RIGHT);
    }

    public boolean rotateCamAntiClock()
    {
        return Gdx.input.isKeyPressed(Actions.ROTATE_CAMERA_ANTICLOCKWISE);
    }

    public boolean moveCamForward()
    {
        return Gdx.input.isKeyPressed(Actions.MOVE_CAMERA_FORWARD);
    }

    public boolean rotateCamClock()
    {
        return Gdx.input.isKeyPressed(Actions.ROTATE_CAMERA_CLOCKWISE);
    }

    public boolean moveBall()
    {
        return Gdx.input.isKeyPressed(Actions.MOVE_BALL);
    }
    public boolean moveBall2()
    {
        return Gdx.input.isKeyPressed(Actions.MOVE_BALL_2);
    }

    @Override
    public Vector3 getDirectionVector() {
        return null;
    }
}