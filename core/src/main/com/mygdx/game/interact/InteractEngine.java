package com.mygdx.game.interact;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.Ingredient;
import com.mygdx.game.player.Player;
import com.mygdx.game.player.PlayerEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * 
 * @author Thomas McCarthy
 * 
 * The Interact Engine renders the various stations in the kitchen, alongside
 * the interaction UI and the timers
 *
 */

public class InteractEngine {

	// An array of interactable objects on the screen
	static Interactable[] interactables;

	// Determines how far away the player must be to interact with a station
	static float interactRange;

	// Textures used for rendering the Progress Slider
	static Texture sliderBackground;
	static Texture sliderFill;


	//==========================================================\\
	//                      INITIALISER                         \\
	//==========================================================\\

	public static void initialise(HashMap<String, InteractableType> interactableTypeHashMap,
		HashMap<InteractableType, ArrayList<Combination>> combinationsHashmap,
		HashMap<InteractableType, HashMap<Ingredient, Action>> actionHashmap)
	{

		InteractableType counter = interactableTypeHashMap.get("counter");
		InteractableType customerCounter = interactableTypeHashMap.get("customer-counter");
		InteractableType bin = interactableTypeHashMap.get("bin");
		InteractableType bunPantry = interactableTypeHashMap.get("bun-pantry");
		InteractableType lettucePantry = interactableTypeHashMap.get("lettuce-pantry");
		InteractableType onionPantry = interactableTypeHashMap.get("onion-pantry");
		InteractableType pattyPantry = interactableTypeHashMap.get("patty-pantry");
		InteractableType tomatoPantry = interactableTypeHashMap.get("tomato-pantry");
		InteractableType bakingStation = interactableTypeHashMap.get("baking-station");
		InteractableType hobStation = interactableTypeHashMap.get("hob-station");
		InteractableType cuttingStation = interactableTypeHashMap.get("cutting-station");

		InteractableInLevel[] interactableInLevels = new InteractableInLevel[] {
			customerCounter.instantiate(1, 2, combinationsHashmap.get(customerCounter), actionHashmap.get(customerCounter)),
			customerCounter.instantiate(1, 3, combinationsHashmap.get(customerCounter), actionHashmap.get(customerCounter)),
			customerCounter.instantiate(1, 4, combinationsHashmap.get(customerCounter), actionHashmap.get(customerCounter)),

			counter.instantiate(1, 5, combinationsHashmap.get(counter), actionHashmap.get(counter)),
			counter.instantiate(1, 1, combinationsHashmap.get(counter), actionHashmap.get(counter)),
			counter.instantiate(6, 6, combinationsHashmap.get(counter), actionHashmap.get(counter)),
			counter.instantiate(6, 0, combinationsHashmap.get(counter), actionHashmap.get(counter)),
			counter.instantiate(7, 6, combinationsHashmap.get(counter), actionHashmap.get(counter)),
			counter.instantiate(7, 0, combinationsHashmap.get(counter), actionHashmap.get(counter)),

			hobStation.instantiate(2, 6, combinationsHashmap.get(hobStation), actionHashmap.get(hobStation)),
			hobStation.instantiate(4, 6, combinationsHashmap.get(hobStation), actionHashmap.get(hobStation)),
			bakingStation.instantiate(3, 6, combinationsHashmap.get(bakingStation), actionHashmap.get(bakingStation)),
			bakingStation.instantiate(5, 6, combinationsHashmap.get(bakingStation), actionHashmap.get(bakingStation)),
			cuttingStation.instantiate(2, 0, combinationsHashmap.get(cuttingStation), actionHashmap.get(cuttingStation)),
			cuttingStation.instantiate(3, 0, combinationsHashmap.get(cuttingStation), actionHashmap.get(cuttingStation)),
			cuttingStation.instantiate(4, 0, combinationsHashmap.get(cuttingStation), actionHashmap.get(cuttingStation)),
			cuttingStation.instantiate(5, 0, combinationsHashmap.get(cuttingStation), actionHashmap.get(cuttingStation)),

			counter.instantiate(3, 3, combinationsHashmap.get(counter), actionHashmap.get(counter)),
			counter.instantiate(4, 3, combinationsHashmap.get(counter), actionHashmap.get(counter)),

			pattyPantry.instantiate(5, 3, combinationsHashmap.get(pattyPantry), actionHashmap.get(pattyPantry)),
			bunPantry.instantiate(6, 3, combinationsHashmap.get(bunPantry), actionHashmap.get(bunPantry)),
			lettucePantry.instantiate(8, 3, combinationsHashmap.get(lettucePantry), actionHashmap.get(lettucePantry)),
			tomatoPantry.instantiate(8, 4, combinationsHashmap.get(tomatoPantry), actionHashmap.get(tomatoPantry)),
			onionPantry.instantiate(8, 2, combinationsHashmap.get(onionPantry), actionHashmap.get(onionPantry)),

			bin.instantiate(8, 6, combinationsHashmap.get(bin), actionHashmap.get(bin)),
			bin.instantiate(8, 0, combinationsHashmap.get(bin), actionHashmap.get(bin)),
		};

		interactables = Arrays.stream(interactableInLevels).map(InteractableInLevel::initialise).toArray(Interactable[]::new);

		interactRange = 85f;

		sliderBackground = new Texture("textures/slider_background.png");
		sliderFill = new Texture("textures/slider_fill.png");

		Rectangle[] collisionRects = new Rectangle[interactables.length];
		for(int i=0; i<interactables.length; i++)
		{
			collisionRects[i] = interactables[i].getCollisionRect();
		}
		PlayerEngine.setColliders(collisionRects);
	}
	
	
	//==========================================================\\
	//                         UPDATE                           \\
	//==========================================================\\

	public static void render(PolygonSpriteBatch batch) {
		for(Interactable interactable : interactables) {
			// Render a progress slider above the element if it is currently preparing
			if(interactable.isPreparing())
			{
				float progressWidth = (float) (interactable.getCurrentTime() / interactable.getPreparationTime() * 0.65);
				batch.draw(sliderBackground, interactable.getXPos(), interactable.getYPos() + 0.7f, 0.7f, 0.2f);
				batch.draw(sliderFill, interactable.getXPos(), interactable.getYPos() + 0.725f, progressWidth, 0.15f);
			}
		}
	}

	//==========================================================\\
	//                   INTERACTION CHECK                      \\
	//==========================================================\\

	/*
		Checks for and initiates an interaction if possible.
		This script is NOT responsible for checking for user input.
		User input is handled by the PlayerEngine, which calls this
		method when required.
		Since the PlayerEngine updates before the InteractEngine,
		the interaction is registered on the frame that the key
		is pressed.
		See PlayerEngine > update() for more.
	 */
}